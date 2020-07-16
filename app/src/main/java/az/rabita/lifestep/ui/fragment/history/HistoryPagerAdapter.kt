package az.rabita.lifestep.ui.fragment.history

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import az.rabita.lifestep.ui.fragment.history.page.HistoryPageType
import az.rabita.lifestep.ui.fragment.history.page.PageHistoryFragment

class HistoryPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    private val fragments = listOf(
        PageHistoryFragment(HistoryPageType.DONATIONS),
        PageHistoryFragment(HistoryPageType.TRANSFERS),
        PageHistoryFragment(HistoryPageType.EARNED)
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

}