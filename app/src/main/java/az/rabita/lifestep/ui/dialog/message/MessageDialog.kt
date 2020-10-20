package az.rabita.lifestep.ui.dialog.message

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.DialogMessageBinding
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.ui.dialog.SingleInstanceDialog

object MessageDialog : SingleInstanceDialog() {

    private lateinit var message: Message

    fun getInstance(message: Message): MessageDialog {
        this.message = message
        return this
    }

    private lateinit var binding: DialogMessageBinding
    private lateinit var sharedPreferences: PreferenceManager

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
        defaultConfigurations()
        bindUI()
    }

    private fun defaultConfigurations() {
        sharedPreferences = PreferenceManager.getInstance(requireContext())
    }

    private fun bindUI(): Unit = with(binding) {

        imageViewIllustration.setImageResource(
            when (message.type) {
                MessageType.NO_INTERNET -> R.drawable.img_no_internet
                MessageType.ERROR -> when (sharedPreferences.langCode) {
                    10 -> R.drawable.img_error_az
                    20 -> R.drawable.img_error_az //TODO error images
                    30 -> R.drawable.img_error_az
                    else -> 0
                }
                MessageType.GOOGLE_FIT_NOT_DOWNLOADED, MessageType.GOOGLE_FIT_NOT_CONNECTED -> R.drawable.ic_google_fit
            }
        )

        textViewMessage.text = message.content

        buttonClose.setOnClickListener {
            dismiss()
        }

    }

}