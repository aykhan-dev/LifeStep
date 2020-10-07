package az.rabita.lifestep.ui.dialog.message

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.DialogMessageBinding
import az.rabita.lifestep.ui.dialog.SingleInstanceDialog

class MessageDialogRefactored(private val type: MessageType) : SingleInstanceDialog() {

    private lateinit var binding: DialogMessageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return DialogMessageBinding.inflate(inflater).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
    }

    private fun bindUI(): Unit = with(binding) {
        imageViewIllustration.setImageResource(
            when (type) {
                MessageType.NO_INTERNET -> R.drawable.img_no_internet
                MessageType.ERROR -> R.drawable.img_error_az
            }
        )
    }

}