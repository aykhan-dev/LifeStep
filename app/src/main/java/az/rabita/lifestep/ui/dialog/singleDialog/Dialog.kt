package az.rabita.lifestep.ui.dialog.singleDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import az.rabita.lifestep.databinding.FragmentCongratsDialogBinding
import az.rabita.lifestep.databinding.FragmentMessageDialogBinding
import az.rabita.lifestep.databinding.LayoutDialogBinding
import az.rabita.lifestep.ui.dialog.SingleInstanceDialog

sealed class DialogType {

    object NoInternetConnection : DialogType()

    data class Message(val msg: String) : DialogType()

    data class Congrats(val isForBonus: Boolean) : DialogType()

}

class Dialog(private val type: DialogType) : SingleInstanceDialog() {

    private lateinit var binding: LayoutDialogBinding
    private lateinit var content: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        content = when (type) {
            is DialogType.NoInternetConnection, is DialogType.Message -> {
                FragmentMessageDialogBinding.inflate(inflater).root
            }
            is DialogType.Congrats -> {
                FragmentCongratsDialogBinding.inflate(inflater).root
            }
        }
        return LayoutDialogBinding.inflate(inflater).also { binding = it }.root
    }

}