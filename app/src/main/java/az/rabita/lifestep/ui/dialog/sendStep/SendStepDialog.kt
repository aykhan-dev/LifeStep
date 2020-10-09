package az.rabita.lifestep.ui.dialog.sendStep

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
import az.rabita.lifestep.databinding.DialogSendStepBinding
import az.rabita.lifestep.ui.dialog.SingleInstanceDialog

class SendStepDialog : SingleInstanceDialog() {

    companion object {
        const val RESULT_KEY = "step sending details"
        const val AMOUNT_KEY = "amount"
    }

    private lateinit var binding: DialogSendStepBinding

    private val args by navArgs<SendStepDialogArgs>()

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return DialogSendStepBinding.inflate(inflater).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
    }

    private fun bindUI(): Unit = with(binding) {

        editTextAmount.hint = args.profileInfo.balance.toString()

        editTextAmount.doAfterTextChanged {
            it?.let { amount ->
                if (amount.isNotEmpty()) {
                    if (amount.toString().toLong() > args.profileInfo.balance ?: 0L) {
                        with(editTextAmount) {
                            text = SpannableStringBuilder(
                                args.profileInfo.balance?.toString() ?: ""
                            )
                            setSelection(text.length)
                        }
                    }
                }
            }
        }

        buttonSendSteps.setOnClickListener {
            if (editTextAmount.text.toString().isNotEmpty()) {
                navController.previousBackStackEntry!!.savedStateHandle.set(
                    RESULT_KEY,
                    mapOf(AMOUNT_KEY to editTextAmount.text.toString().toLong())
                )
            }
            navController.popBackStack()
        }

    }

}