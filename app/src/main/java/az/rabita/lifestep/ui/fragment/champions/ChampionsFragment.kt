package az.rabita.lifestep.ui.fragment.champions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentChampionsBinding
import az.rabita.lifestep.ui.fragment.champions.page.ChampionsPageFragment
import az.rabita.lifestep.ui.fragment.champions.page.ChampionsPageType
import az.rabita.lifestep.utils.lib.PagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class ChampionsFragment : Fragment() {

    private lateinit var binding: FragmentChampionsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FragmentChampionsBinding.inflate(inflater).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
        configureViewPager()
        configureTabLayout()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@ChampionsFragment

        imageButtonBack.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun configureViewPager(): Unit = with(binding.viewPager) {
        val pages = listOf<Fragment>(
            ChampionsPageFragment(ChampionsPageType.DAILY),
            ChampionsPageFragment(ChampionsPageType.WEEKLY),
            ChampionsPageFragment(ChampionsPageType.MONTHLY)
        )
        adapter = PagerAdapter(requireActivity(), pages)
    }

    private fun configureTabLayout(): Unit = with(binding) {
        val titles = requireContext().resources.getStringArray(R.array.championsPageTabs)
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

}