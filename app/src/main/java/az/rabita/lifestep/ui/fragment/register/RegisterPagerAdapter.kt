package az.rabita.lifestep.ui.fragment.register

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import az.rabita.lifestep.ui.fragment.register.parts.first.FirstRegisterFragment
import az.rabita.lifestep.ui.fragment.register.parts.second.SecondRegisterFragment

class RegisterPagerAdapter(listenerTwo: () -> Unit, fa: FragmentActivity) : FragmentStateAdapter(fa) {

    private val fragments = listOf(FirstRegisterFragment(), SecondRegisterFragment(listenerTwo))

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

}