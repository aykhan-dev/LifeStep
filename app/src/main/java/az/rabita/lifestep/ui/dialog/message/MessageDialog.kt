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
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.utils.LANG_AZ
import az.rabita.lifestep.utils.LANG_KEY

class MessageDialog(
    private val message: String
) : DialogFragment() {

    private lateinit var binding: FragmentMessageDialogBinding

    private lateinit var sharedPreferences: PreferenceManager

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

        sharedPreferences = PreferenceManager.getInstance(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
    }

    private fun bindUI(): Unit = with(binding) {

        imageViewIllustration.setImageResource(
            when (message) {
                getString(R.string.no_internet_connection) -> R.drawable.img_no_internet
                getString(R.string.google_auth_fail_message) -> R.drawable.ic_google_fit
                else -> {
                    when (sharedPreferences.getIntegerElement(LANG_KEY, LANG_AZ)) {
                        10 -> R.drawable.img_error_az
                        20 -> R.drawable.img_error_az
                        30 -> R.drawable.img_error_az
                        else -> 0
                    }
                }
            }
        )

        content.startAnimation(openAnimation)

        lifecycleOwner = this@MessageDialog
        errorMessage = message

        root.setOnClickListener { dismiss() }
        buttonClose.setOnClickListener { dismiss() }
    }

    override fun getTheme(): Int = R.style.DialogTheme

}
