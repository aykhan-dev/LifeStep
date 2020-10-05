package az.rabita.lifestep.ui.dialog.donateStep

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.DialogDonateStepBinding
import az.rabita.lifestep.ui.dialog.SingleInstanceDialog
import com.bumptech.glide.Glide

class DonateStepDialogRefactored : SingleInstanceDialog() {

    private lateinit var binding: DialogDonateStepBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return DialogDonateStepBinding.inflate(inflater).also { binding = it }.root
    }

}