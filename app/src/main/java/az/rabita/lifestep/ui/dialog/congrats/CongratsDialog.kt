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
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentCongratsDialogBinding

class CongratsDialog : DialogFragment() {

    private lateinit var binding: FragmentCongratsDialogBinding

    private val navController by lazy { findNavController() }

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@CongratsDialog

        binding.content.startAnimation(openAnimation)

        root.setOnClickListener {
            val savedState = navController.previousBackStackEntry!!.savedStateHandle
            savedState.set("Donated", true)
            navController.popBackStack()
        }
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

}