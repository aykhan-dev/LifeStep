package az.rabita.lifestep.ui.dialog.donateStep

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import az.rabita.lifestep.databinding.DialogDonateStepBinding
import az.rabita.lifestep.ui.dialog.SingleInstanceDialog

class DonateStepDialogRefactored : SingleInstanceDialog() {

    private lateinit var binding: DialogDonateStepBinding

    private val navController by lazy { findNavController() }

    private val args by navArgs<DonateStepDialogRefactoredArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return DialogDonateStepBinding.inflate(inflater).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
    }

    private fun bindUI(): Unit = with(binding) {

        editTextAmount.hint = args.ownProfileInfo.balance.toString()

        editTextAmount.doAfterTextChanged {
            it?.let { amount ->
                if (amount.isNotEmpty()) {
                    if (amount.toString().toLong() > args.ownProfileInfo.balance ?: 0L) {
                        with(editTextAmount) {
                            text = SpannableStringBuilder(
                                args.ownProfileInfo.balance?.toString() ?: ""
                            )
                            setSelection(text.length)
                        }
                    }
                }
            }
        }

        buttonDonateSteps.setOnClickListener {
            if (editTextAmount.text.toString().isNotEmpty()) {
                navController.previousBackStackEntry!!.savedStateHandle.set(
                    "donation details",
                    mapOf(
                        "amount" to editTextAmount.text.toString().toLong(),
                        "isPrivate" to switcher.isSelected
                    )
                )
            }
            navController.popBackStack()
        }

        switcher.setOnSwitchListener {
            navController.previousBackStackEntry!!.savedStateHandle.set(
                "isPrivate",
                it
            )
        }

    }

}