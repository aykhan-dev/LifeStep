package az.rabita.lifestep.ui.fragment.detailedInfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import az.rabita.lifestep.NavGraphMainDirections
import az.rabita.lifestep.databinding.FragmentDetailedInfoBinding
import az.rabita.lifestep.ui.dialog.message.SingleMessageDialog
import az.rabita.lifestep.ui.fragment.ranking.RankingRecyclerAdapter
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.STEP_DONATED_RESULT
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.utils.moreShortenString
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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeStates()
        observeEvents()
    }

    override fun onStart() {
        super.onStart()
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

    private fun navigateTo(direction: DetailedFragmentDirections) {
        when (direction) {
            DetailedFragmentDirections.DONATE_STEPS -> {
                viewModel.assocationDetails.value?.let {
                    uiInitialState()
                    navController.navigate(
                        DetailedInfoFragmentDirections.actionDetailedInfoFragmentToDonateStepDialog(
                            it.id,
                            args.assocationId
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
                activity?.let { activity ->
                    SingleMessageDialog.popUp(
                        activity.supportFragmentManager,
                        ERROR_TAG,
                        errorMsg
                    )
                }
            }
        })

    }

    private fun observeStates(): Unit = with(viewModel) {}

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

    private fun uiInitialState(): Unit = with(binding) {
        binding.scrollContent.smoothScrollTo(0, 0)
        binding.motionLayout.transitionToStart()
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
