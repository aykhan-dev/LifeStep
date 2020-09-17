package az.rabita.lifestep.utils

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import az.rabita.lifestep.ui.dialog.message.RefactoredMessageDialog

object Message {

    private lateinit var dialogInstance: RefactoredMessageDialog

    fun getInstance(): RefactoredMessageDialog {
        if (!::dialogInstance.isInitialized) dialogInstance = RefactoredMessageDialog()
        return dialogInstance
    }

}

fun DialogFragment.show(fragmentManager: FragmentManager, tag: String) {
    if (dialog?.isShowing == false) show(fragmentManager, tag)
}