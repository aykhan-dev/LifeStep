package az.rabita.lifestep.ui.fragment.friends.page

import androidx.recyclerview.widget.DiffUtil
import az.rabita.lifestep.pojo.apiPOJO.content.FriendContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.entity.Friend

object FriendDiffCallback : DiffUtil.ItemCallback<FriendContentPOJO>() {

    override fun areItemsTheSame(oldItem: FriendContentPOJO, newItem: FriendContentPOJO): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FriendContentPOJO, newItem: FriendContentPOJO): Boolean {
        return oldItem == newItem
    }

}