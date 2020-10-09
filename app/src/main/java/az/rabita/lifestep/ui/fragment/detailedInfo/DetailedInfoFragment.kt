package az.rabita.lifestep.ui.fragment.detailedInfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import az.rabita.lifestep.NavGraphMainDirections
import az.rabita.lifestep.databinding.FragmentDetailedInfoBinding
import az.rabita.lifestep.pojo.dataHolder.UserProfileInfoHolder
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.fragment.ranking.RankingRecyclerAdapter
import az.rabita.lifestep.utils.*
import az.rabita.lifestep.viewModel.fragment.detailedInfo.DetailedInfoViewModel
import jp.wasabeef.recyclerview.animators.ScaleInAnimator

class DetailedInfoFragment : Fragment() {

    private lateinit var binding: FragmentDetailedInfoBinding

    private val viewModel by viewModels<DetailedInfoViewModel>()
    private val args by navArgs<DetailedInfoFragmentArgs>()

    private val donorsAdapter = RankingRecyclerAdapter { ranker ->
        viewModel.profileInfo.value?.let { info ->
            if (info.id == ranker.id) navController.navigate(NavGraphMainDirections.actionToOwnProfileFragment())
            else navController.navigate(NavGraphMainDirections.actionToOtherProfileFragment(ranker.id))
        }
    }

    private val navController by lazy { findNavController() }

    private val loadingDialog = LoadingDialog()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailedInfoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
        configureRecyclerView()
        retrieveStepDonationDetails()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeStates()
        observeEvents()
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchPersonalInfo()
        viewModel.fetchDetailedInfo(args.assocationId)
        checkDonationResult()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@DetailedInfoFragment
        viewModel = this@DetailedInfoFragment.viewModel

        imageButtonBack.setOnClickListener { activity?.onBackPressed() }
        buttonDonateSteps.setOnClickListener { navigateTo(DetailedFragmentDirections.DONATE_STEPS) }
        textViewAll.setOnClickListener { navigateTo(DetailedFragmentDirections.ALL_DONORS) }
    }

    private fun retrieveStepDonationDetails() {
        navController.currentBackStackEntry!!.savedStateHandle.getLiveData<Map<String, Any>>("donation details")
            .observe(viewLifecycleOwner, Observer {
                it?.let { data ->
                    viewModel.profileInfo.value?.let { info ->
                        val amount = data["amount"] as Long
                        val isPrivate = data["isPrivate"] as Boolean
                        viewModel.donateStep(info.id, amount, isPrivate)
                    }
                }
            })
    }

    private fun navigateTo(direction: DetailedFragmentDirections) {
        when (direction) {
            DetailedFragmentDirections.DONATE_STEPS -> {
                viewModel.profileInfo.value?.let {
                    navController.navigate(
                        DetailedInfoFragmentDirections.actionDetailedInfoFragmentToDonateStepDialogRefactored(
                            UserProfileInfoHolder(it.id, it.balance)
                        )
                    )
                }
            }
            DetailedFragmentDirections.ALL_DONORS -> {
                viewModel.assocationDetails.value?.let {
                    navController.navigate(
                        DetailedInfoFragmentDirections.actionDetailedInfoFragmentToRankingFragment(
                            postId = it.id
                        )
                    )
                }
            }
        }
    }

    private fun configureRecyclerView(): Unit = with(binding.recyclerViewDonors) {
        adapter = donorsAdapter
        itemAnimator = ScaleInAnimator().apply { addDuration = 100 }
    }

    private fun observeData(): Unit = with(viewModel) {

        profileInfo.observe(viewLifecycleOwner, {}) //DON'T REMOVE THIS LINE ELSE IT WILL BE NULL

        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("Donated")
            ?.observe(viewLifecycleOwner, {
                viewModel.fetchDetailedInfo(args.assocationId)
            })

        assocationDetails.observe(viewLifecycleOwner, {
            it?.let {
                with(binding) {
                    textViewDonatedCount.text = it.balance.moreShortenString()
                    textViewTargetCount.text = it.stepNeed.moreShortenString()
                }
            }
        })

        topDonorsList.observe(viewLifecycleOwner, {
            it?.let {
                donorsAdapter.submitList(it)
            }
        })

        errorMessage.observe(viewLifecycleOwner, {
            it?.let { errorMsg ->
                MessageDialog.getInstance(errorMsg).show(
                    requireActivity().supportFragmentManager,
                    ERROR_TAG
                )
            }
        })

    }

    private fun observeStates(): Unit = with(viewModel) {

        uiState.observe(viewLifecycleOwner, {
            it?.let {
                when (it) {
                    is UiState.Loading -> loadingDialog.show(
                        requireActivity().supportFragmentManager,
                        ERROR_TAG
                    )
                    is UiState.LoadingFinished -> {
                        loadingDialog.dismiss()
                        uiState.value = null
                    }
                }
            }
        })

    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventShowDonateStepsDialog.observe(viewLifecycleOwner, {
            it?.let {
                if (it) navigateTo(DetailedFragmentDirections.DONATE_STEPS)
            }
        })

        eventExpiredToken.observe(viewLifecycleOwner, {
            it?.let {
                if (it) {
                    activity?.logout()
                    endExpireTokenProcess()
                }
            }
        })

    }

    private fun checkDonationResult() {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            STEP_DONATED_RESULT
        )?.observe(viewLifecycleOwner, { result ->
            if (result) {
                navController.navigate(NavGraphMainDirections.actionToCongratsDialog())
            }
        })
    }

}
