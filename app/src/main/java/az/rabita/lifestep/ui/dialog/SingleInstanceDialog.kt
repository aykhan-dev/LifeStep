package az.rabita.lifestep.ui.dialog

import android.content.DialogInterface
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

open class SingleInstanceDialog : DialogFragment() {

    private var isOnScreen = false

    override fun show(manager: FragmentManager, tag: String?) {
        if (!isOnScreen) {
            super.show(manager, tag)
            isOnScreen = true
        }
    }

    override fun dismiss() {
        super.dismiss()
        isOnScreen = false
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        isOnScreen = false
    }

}