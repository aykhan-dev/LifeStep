package az.rabita.lifestep.ui.fragment.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.NavGraphMainDirections
import az.rabita.lifestep.databinding.FragmentAdsBinding
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.SingleMessageDialog
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

        adsTransaction.observe(viewLifecycleOwner, {
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

        uiState.observe(viewLifecycleOwner, {
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

        eventExpiredToken.observe(viewLifecycleOwner, {
            it?.let {
                if (it) {
                    activity?.logout()
                    endExpireTokenProcess()
                }
            }
        })

    }

}
