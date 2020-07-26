package az.rabita.lifestep.ui.fragment.notifications

import androidx.recyclerview.widget.DiffUtil
import az.rabita.lifestep.pojo.apiPOJO.entity.Notification
import az.rabita.lifestep.pojo.apiPOJO.entity.Notifications

object NotificationDiffCallback : DiffUtil.ItemCallback<Notifications>() {

    override fun areItemsTheSame(
        oldItem: Notifications,
        newItem: Notifications
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Notifications,
        newItem: Notifications
    ): Boolean {
        return oldItem == newItem
    }

}