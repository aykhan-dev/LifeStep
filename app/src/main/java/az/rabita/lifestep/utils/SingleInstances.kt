package az.rabita.lifestep.utils

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import az.rabita.lifestep.ui.dialog.message.RefactoredMessageDialog

object Message {

    private lateinit var dialogInstance: RefactoredMessageDialog

    private var isShowing = false

    fun getInstance(): RefactoredMessageDialog {
        if (!::dialogInstance.isInitialized) dialogInstance = RefactoredMessageDialog()
        return dialogInstance
    }

    fun showControlled(fragmentManager: FragmentManager, tag: String): Unit = with(dialogInstance) {
        if (!isShowing) {
            show(fragmentManager, tag)
            isShowing = true
        }
    }

    fun dismiss() {
        if (!::dialogInstance.isInitialized) {
            dialogInstance.dismiss()
            isShowing = false
        }
    }

}