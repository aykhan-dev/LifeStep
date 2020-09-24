package az.rabita.lifestep.ui.dialog.sendStep

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentSendStepDialogBinding
import az.rabita.lifestep.ui.dialog.message.SingleMessageDialog
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.hideKeyboard
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.sendStep.SendStepViewModel

class SendStepDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentSendStepDialogBinding

    private val openAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.fade_in)
    }

    private val viewModel by viewModels<SendStepViewModel>()
    private val args by navArgs<SendStepDialogFragmentArgs>()

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.let {
            it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        binding = FragmentSendStepDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.personalInfo = args.profileInfo

        bindUI()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@SendStepDialogFragment
        viewModel = this@SendStepDialogFragment.viewModel

        binding.content.startAnimation(openAnimation)

        buttonSendSteps.setOnClickListener {
            this@SendStepDialogFragment.viewModel.sendStep(args.profileInfo.userId)
        }
        root.setOnClickListener { dismiss() }
        content.setOnClickListener { it.hideKeyboard(context) }
    }

    private fun observeData(): Unit = with(viewModel) {

        errorMessage.observe(viewLifecycleOwner, Observer {
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

    private fun observeEvents(): Unit = with(viewModel) {

        eventDismissDialog.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) navController.navigate(
                    SendStepDialogFragmentDirections.actionSendStepDialogToOtherUserProfileFragment(
                        args.profileInfo.userId
                    )
                )
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