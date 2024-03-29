package az.rabita.lifestep.ui.fragment.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.NavGraphMainDirections
import az.rabita.lifestep.databinding.FragmentAdsBinding
import az.rabita.lifestep.ui.dialog.ads.AdsDialog
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.utils.*
import az.rabita.lifestep.viewModel.fragment.ads.AdsViewModel

class AdsFragment : Fragment() {

    private lateinit var binding: FragmentAdsBinding

    private val viewModel by viewModels<AdsViewModel>()

    private val navController by lazy { findNavController() }

    private val loadingDialog = LoadingDialog()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeStates()
        observeEvents()
        retrieveAdsResults()
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchAdsPageContent()
    }

    private fun bindUI() = with(binding) {
        lifecycleOwner = this@AdsFragment
        viewModel = this@AdsFragment.viewModel

        buttonWatchAds.setOnClickListener {
            this@AdsFragment.viewModel.createAdsTransaction()
        }
    }

    private fun observeData(): Unit = with(viewModel) {

        errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let { errorMsg ->
                MessageDialog.getInstance(errorMsg).show(
                    requireActivity().supportFragmentManager,
                    ERROR_TAG
                )
            }
        })

        adsTransaction.observe(viewLifecycleOwner, Observer {
            it?.let {
                val dataHolder = it.asAdsTransactionInfoHolderObject()
                navController.navigate(
                    NavGraphMainDirections.actionToAdsDialog(
                        dataHolder,
                        true
                    )
                )
            }
        })

    }

    private fun observeStates(): Unit = with(viewModel) {

        uiState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    is UiState.Loading -> {
                        activity?.let { activity ->
                            loadingDialog.show(activity.supportFragmentManager, LOADING_TAG)
                        }
                    }
                    is UiState.LoadingFinished -> loadingDialog.dismiss()
                }
            }
        })

    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventExpiredToken.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    activity?.logout()
                    endExpireTokenProcess()
                }
            }
        })

        eventShowCongratsDialog.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) navController.navigate(NavGraphMainDirections.actionToBonusStepDialog())
            }
        })

    }

    private fun retrieveAdsResults() {

        val stateHandle = navController.currentBackStackEntry!!.savedStateHandle

        stateHandle.getLiveData<Map<String, Any>>(AdsDialog.RESULT_KEY)
            .observe(viewLifecycleOwner, Observer { it?.let(viewModel::getBonusSteps) })

    }

}
