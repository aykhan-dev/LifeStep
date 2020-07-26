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
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.ui.fragment.ranking.RankingRecyclerAdapter
import az.rabita.lifestep.utils.*
import az.rabita.lifestep.viewModel.fragment.detailedInfo.DetailedInfoViewModel

class DetailedInfoFragment : Fragment() {

    private lateinit var binding: FragmentDetailedInfoBinding

    private val viewModel: DetailedInfoViewModel by viewModels()

    private val args: DetailedInfoFragmentArgs by navArgs()

    private val donorsAdapter by lazy {
        RankingRecyclerAdapter { ranker ->
            navController.navigate(
                DetailedInfoFragmentDirections.actionDetailedInfoFragmentToUserProfileFragment(
                    ranker.id
                )
            )
        }
    }

    private val navController by lazy { findNavController() }

    private val loadingDialog by lazy { LoadingDialog() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailedInfoBinding.inflate(inflater)

        binding.apply {
            lifecycleOwner = this@DetailedInfoFragment
            viewModel = this@DetailedInfoFragment.viewModel
        }

        with(binding) {
            imageButtonBack.setOnClickListener { activity?.onBackPressed() }
            buttonDonateSteps.setOnClickListener { navigateTo(DetailedFragmentDirections.DONATE_STEPS) }
            textViewAll.setOnClickListener { navigateTo(DetailedFragmentDirections.ALL_DONORS) }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchDetailedInfo(args.assocationId)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeStates()
        observeEvents()
    }

    private fun navigateTo(direction: DetailedFragmentDirections) = when (direction) {
        DetailedFragmentDirections.DONATE_STEPS -> {
            viewModel.assocationDetails.value?.let {
                navController.navigate(
                    DetailedInfoFragmentDirections.actionDetailedInfoFragmentToDonateStepDialog(
                        it.id
                    )
                )
            }
        }
        DetailedFragmentDirections.CONGRATS -> {
        }
        DetailedFragmentDirections.ALL_DONORS -> {
            navController.navigate(
                DetailedInfoFragmentDirections.actionDetailedInfoFragmentToRankingFragment(
                    postId = viewModel.assocationDetails.value?.id
                )
            )
        }
    }

    private fun configureRecyclerView(): Unit = with(binding.recyclerViewDonors) {
        adapter = donorsAdapter

        context?.let {
            addItemDecoration(
                VerticalSpaceItemDecoration(0, pxFromDp(it, 90f))
            )
        }
    }

    private fun observeData(): Unit = with(viewModel) {

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
                    MessageDialog(MessageType.ERROR, it).show(
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
