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
import az.rabita.lifestep.databinding.FragmentDetailedInfoBinding
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
        navController.navigate(
            DetailedInfoFragmentDirections.actionDetailedInfoFragmentToUserProfileFragment(
                ranker.id
            )
        )
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
                    navController.navigate(
                        DetailedInfoFragmentDirections.actionDetailedInfoFragmentToDonateStepDialog(
                            it.id
                        )
                    )
                }
            }
            DetailedFragmentDirections.ALL_DONORS -> {
                navController.navigate(
                    DetailedInfoFragmentDirections.actionDetailedInfoFragmentToRankingFragment(
                        postId = viewModel.assocationDetails.value?.id
                    )
                )
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
            ?.observe(viewLifecycleOwner, Observer {
                viewModel.fetchDetailedInfo(args.assocationId)
            })

        assocationDetails.observe(viewLifecycleOwner, Observer {
            it?.let {
                with(binding) {
                    textViewDonatedCount.text = it.balance.moreShortenString()
                    textViewTargetCount.text = it.stepNeed.moreShortenString()
                }
            }
        })

        topDonorsList.observe(viewLifecycleOwner, Observer {
            it?.let {
                donorsAdapter.submitList(it)
            }
        })

        errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let {
                activity?.let { activity ->
                    MessageDialog(it).show(
                        activity.supportFragmentManager,
                        ERROR_TAG
                    )
                }
            }
        })

    }

    private fun observeStates(): Unit = with(viewModel) {

        uiState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    is UiState.Loading -> activity?.supportFragmentManager?.let { fm ->
                        loadingDialog.show(
                            fm,
                            LOADING_TAG
                        )
                    }
                    is UiState.LoadingFinished -> {
                        loadingDialog.dismiss()
                        uiState.value = null
                    }
                }
            }
        })

    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventShowDonateStepsDialog.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) navigateTo(DetailedFragmentDirections.DONATE_STEPS)
            }
        })

        eventExpiredToken.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    activity?.logout()
                    endExpireTokenProcess()
                }
            }
        })

    }

}
