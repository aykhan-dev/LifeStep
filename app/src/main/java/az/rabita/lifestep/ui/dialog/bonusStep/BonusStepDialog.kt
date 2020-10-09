package az.rabita.lifestep.ui.dialog.bonusStep

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import az.rabita.lifestep.databinding.DialogBonusStepBinding
import az.rabita.lifestep.ui.dialog.SingleInstanceDialog

class BonusStepDialog : SingleInstanceDialog() {

    private lateinit var binding: DialogBonusStepBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return DialogBonusStepBinding.inflate(inflater).also { binding = it }.root
    }

}