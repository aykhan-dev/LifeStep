package az.rabita.lifestep.ui.fragment.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.databinding.ItemNotificationBinding
import az.rabita.lifestep.pojo.apiPOJO.entity.Notifications

class NotificationViewHolder private constructor(private val binding: ItemNotificationBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        notificationContentPOJO: Notifications,
        clickListener: (notification: Notifications) -> Unit
    ) =
        with(binding) {
            data = notificationContentPOJO
            root.setOnClickListener { clickListener(notificationContentPOJO) }
        }

    companion object {
        fun from(viewGroup: ViewGroup): NotificationViewHolder {
            val inflater = LayoutInflater.from(viewGroup.context)
            val binding = ItemNotificationBinding.inflate(inflater, viewGroup, false)
            return NotificationViewHolder(binding)
        }
    }

}