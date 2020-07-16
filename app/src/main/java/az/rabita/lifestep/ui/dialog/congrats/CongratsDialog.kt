package az.rabita.lifestep.ui.dialog.congrats

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
import az.rabita.lifestep.databinding.FragmentCongratsDialogBinding

class CongratsDialog : DialogFragment() {

    private lateinit var binding: FragmentCongratsDialogBinding

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

        binding = FragmentCongratsDialogBinding.inflate(inflater)

        binding.content.startAnimation(openAnimation)

        binding.apply {
            lifecycleOwner = this@CongratsDialog
        }

        with(binding) {
            root.setOnClickListener { dismiss() }
        }

        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

}
