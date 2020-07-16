package az.rabita.lifestep.ui.fragment.notifications

import androidx.recyclerview.widget.DiffUtil
import az.rabita.lifestep.pojo.apiPOJO.entity.Notification

object NotificationDiffCallback : DiffUtil.ItemCallback<Notification>() {

    override fun areItemsTheSame(
        oldItem: Notification,
        newItem: Notification
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Notification,
        newItem: Notification
    ): Boolean {
        return oldItem == newItem
    }

}