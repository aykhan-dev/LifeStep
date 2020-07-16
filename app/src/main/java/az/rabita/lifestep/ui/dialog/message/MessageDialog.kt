package az.rabita.lifestep.ui.dialog.message

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
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentMessageDialogBinding

class MessageDialog(
    private val messageType: MessageType,
    private val message: String
) : DialogFragment() {

    private lateinit var binding: FragmentMessageDialogBinding

    private val openAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.fade_in)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.let {
            it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        binding = FragmentMessageDialogBinding.inflate(inflater)

        binding.content.startAnimation(openAnimation)

        binding.apply {
            lifecycleOwner = this@MessageDialog
            errorMessage = message
        }

        with(binding) {
            root.setOnClickListener { dismiss() }
            buttonClose.setOnClickListener { dismiss() }
        }

        return binding.root

    }

    override fun getTheme(): Int = R.style.DialogTheme

}
