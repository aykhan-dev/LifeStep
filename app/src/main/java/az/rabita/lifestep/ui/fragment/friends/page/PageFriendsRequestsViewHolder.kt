package az.rabita.lifestep.ui.fragment.friends.page

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.databinding.ItemFriendRequestsBinding
import az.rabita.lifestep.pojo.apiPOJO.content.FriendContentPOJO

class PageFriendsRequestsViewHolder private constructor(
    private val binding: ItemFriendRequestsBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        friend: FriendContentPOJO,
        clickListener: (userId: String, isAccepted: Boolean?) -> Unit
    ): Unit = with(binding) {
        data = friend
        buttonAccept.setOnClickListener { clickListener(friend.id, true) }
        buttonDecline.setOnClickListener { clickListener(friend.id, false) }
        root.setOnClickListener { clickListener(friend.id, null) }
    }

    companion object {

        fun from(parent: ViewGroup): PageFriendsRequestsViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemFriendRequestsBinding.inflate(layoutInflater, parent, false)
            return PageFriendsRequestsViewHolder(binding)
        }

    }

}