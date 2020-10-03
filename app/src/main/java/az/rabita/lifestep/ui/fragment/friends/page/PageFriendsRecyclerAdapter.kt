package az.rabita.lifestep.ui.fragment.friends.page

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.pojo.apiPOJO.content.FriendContentPOJO
import az.rabita.lifestep.ui.fragment.friends.FriendsPageType

class PageFriendsRecyclerAdapter(
    private val type: FriendsPageType,
    private val clickListener: (userId: String, isAccepted: Boolean?) -> Unit
) : PagingDataAdapter<FriendContentPOJO, RecyclerView.ViewHolder>(FriendDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (type) {
            FriendsPageType.MY_FRIENDS -> PageMyFriendsViewHolder.from(parent)
            FriendsPageType.FRIEND_REQUESTS -> PageFriendsRequestsViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            when (type) {
                FriendsPageType.MY_FRIENDS -> (holder as PageMyFriendsViewHolder).bind(
                    it,
                    clickListener
                )
                FriendsPageType.FRIEND_REQUESTS -> (holder as PageFriendsRequestsViewHolder).bind(
                    it,
                    clickListener
                )
            }
        }
    }

}