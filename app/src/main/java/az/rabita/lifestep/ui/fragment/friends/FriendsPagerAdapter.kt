package az.rabita.lifestep.ui.fragment.friends

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import az.rabita.lifestep.ui.fragment.friends.page.PageFriendsFragment

class FriendsPagerAdapter(
    fa: FragmentActivity,
    itemClickListener: (id: String) -> Unit
) : FragmentStateAdapter(fa) {

    private val fragments = listOf(
        PageFriendsFragment(FriendsPageType.MY_FRIENDS, itemClickListener),
        PageFriendsFragment(FriendsPageType.FRIEND_REQUESTS, itemClickListener)
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

}