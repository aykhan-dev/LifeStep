package az.rabita.lifestep.ui.dialog.donateStep

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentDonateStepDialogBinding
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.*
import az.rabita.lifestep.viewModel.fragment.detailedInfo.DetailedInfoViewModel

class DonateStepDialog : DialogFragment() {

    private lateinit var binding: FragmentDonateStepDialogBinding;

    private lateinit var viewModel: DetailedInfoViewModel

    private val openAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.fade_in)
    }

    private val args: DonateStepDialogArgs by navArgs()

    private val loadingDialog by lazy { LoadingDialog() }

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.let {
            it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        binding = FragmentDonateStepDialogBinding.inflate(inflater)

        viewModel = ViewModelProvider(this).get(DetailedInfoViewModel::class.java)

        binding.content.startAnimation(openAnimation)

        binding.apply {
            lifecycleOwner = this@DonateStepDialog
            viewModel = this@DonateStepDialog.viewModel
        }

        with(binding) {
            buttonDonateSteps.setOnClickListener {
                this@DonateStepDialog.viewModel.donateStep(
                    args.postId,
                    switcher.isSwitchOn
                )
            }
            root.setOnClickListener { dismiss() }
            content.setOnClickListener { it.hideKeyboard(context) }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchPersonalInfo()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeStates()
        observeEvents()
    }

    private fun observeData(): Unit = with(viewModel) {

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

        eventShowCongratsDialog.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    loadingDialog.dismiss()
                    navController.navigate(DonateStepDialogDirections.actionDonateStepDialogToCongratsDialog())
                }
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

    override fun getTheme(): Int = R.style.DialogTheme

}
