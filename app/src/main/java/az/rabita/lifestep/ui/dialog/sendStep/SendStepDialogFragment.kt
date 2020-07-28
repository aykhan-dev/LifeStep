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
import androidx.navigation.fragment.navArgs
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentSendStepDialogBinding
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.hideKeyboard
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.profileDetails.UserProfileViewModel

class SendStepDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentSendStepDialogBinding

    private val openAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.fade_in)
    }

    private val viewModel: UserProfileViewModel by viewModels()

    private val args: SendStepDialogFragmentArgs by navArgs()

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

        binding.content.startAnimation(openAnimation)

        binding.apply {
            lifecycleOwner = this@SendStepDialogFragment
            viewModel = this@SendStepDialogFragment.viewModel
        }

        with(binding) {
            buttonSendSteps.setOnClickListener {
                this@SendStepDialogFragment.viewModel.sendStep(args.userId)
            }
            root.setOnClickListener { dismiss() }
            content.setOnClickListener { it.hideKeyboard(context) }
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
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

    private fun observeEvents(): Unit = with(viewModel) {

        eventDismissDialog.observe(viewLifecycleOwner, Observer {
            it?.let { if (it) dismiss() }
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