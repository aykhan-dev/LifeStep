package az.rabita.lifestep.ui.dialog.loading

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.DialogLoadingBinding
import az.rabita.lifestep.ui.dialog.SingleInstanceDialog
import com.bumptech.glide.Glide

class LoadingDialog : SingleInstanceDialog() {

    private lateinit var binding: DialogLoadingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isCancelable = false
        return DialogLoadingBinding.inflate(inflater).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(requireContext()).load(R.raw.gif_loading).into(binding.imageView)
    }

}