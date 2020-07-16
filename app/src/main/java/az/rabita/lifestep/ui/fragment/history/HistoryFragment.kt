package az.rabita.lifestep.ui.fragment.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentHistoryBinding
import com.google.android.material.tabs.TabLayoutMediator

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(inflater)

        binding.apply {
            lifecycleOwner = this@HistoryFragment
        }

        with(binding) {
            imageButtonBack.setOnClickListener { navController.popBackStack() }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureViewPager()
    }

    private fun configureViewPager() = with(binding) {
        val tabTitles = listOf(
            getString(R.string.title_donated),
            getString(R.string.title_transfered),
            getString(R.string.title_earned)
        )

        activity?.let { viewPager.adapter = HistoryPagerAdapter(it) }
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

}
