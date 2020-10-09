package az.rabita.lifestep.pojo.holder

import android.os.Parcelable
import az.rabita.lifestep.ui.dialog.message.MessageType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Message(
    val content: String,
    val type: MessageType
) : Parcelable