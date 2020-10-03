package az.rabita.lifestep.ui.fragment.friends.page

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.databinding.ItemMyFriendsBinding
import az.rabita.lifestep.pojo.apiPOJO.content.FriendContentPOJO

class PageMyFriendsViewHolder(
    private val binding: ItemMyFriendsBinding
) : RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("SetTextI18n")
    fun bind(
        friend: FriendContentPOJO,
        clickListener: (userId: String, isAccepted: Boolean?) -> Unit
    ) = with(binding) {
        data = friend
        root.setOnClickListener { clickListener(friend.id, null) }
    }

    companion object {

        fun from(parent: ViewGroup): PageMyFriendsViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemMyFriendsBinding.inflate(layoutInflater, parent, false)
            return PageMyFriendsViewHolder(binding)
        }

    }

}