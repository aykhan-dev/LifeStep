package az.rabita.lifestep.ui.dialog.congrats

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import az.rabita.lifestep.databinding.DialogCongratsBinding
import az.rabita.lifestep.ui.dialog.SingleInstanceDialog

class CongratsDialogRefactored : SingleInstanceDialog() {

    private lateinit var binding: DialogCongratsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return DialogCongratsBinding.inflate(inflater).also { binding = it }.root
    }

}