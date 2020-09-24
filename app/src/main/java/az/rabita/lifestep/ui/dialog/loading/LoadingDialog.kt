package az.rabita.lifestep.ui.dialog.loading

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentLoadingDialogBinding
import az.rabita.lifestep.ui.dialog.SingleInstanceDialog
import com.bumptech.glide.Glide

object LoadingDialog : SingleInstanceDialog() {

    private lateinit var binding: FragmentLoadingDialogBinding

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

        isCancelable = false
        binding = FragmentLoadingDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@LoadingDialog

        binding.content.startAnimation(openAnimation)

        context?.let { Glide.with(it).load(R.raw.gif_loading).into(imageViewLoading) }
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

}
