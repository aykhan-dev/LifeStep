package az.rabita.lifestep.ui.fragment.champions.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import az.rabita.lifestep.databinding.FragmentChampionsPageBinding
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.champions.ChampionsViewModel

class ChampionsPageFragment(private val pageType: ChampionsPageType) : Fragment() {

    private lateinit var binding: FragmentChampionsPageBinding

    private val viewModel by viewModels<ChampionsViewModel>()
    private val listAdapter = ChampionsListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FragmentChampionsPageBinding.inflate(inflater).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
        configureRecyclerView()
    }

    override fun onStart(): Unit = with(viewModel) {
        super.onStart()
        when (pageType) {
            ChampionsPageType.DAILY -> fetchDailyChampions()
            ChampionsPageType.WEEKLY -> fetchWeeklyChampions()
            ChampionsPageType.MONTHLY -> fetchMonthlyChampions()
        }
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@ChampionsPageFragment
    }

    private fun configureRecyclerView(): Unit = with(binding.recyclerViewChampions) {
        adapter = this@ChampionsPageFragment.listAdapter
    }

    private fun observeData(): Unit = with(viewModel) {
        listOfChampions.observe(viewLifecycleOwner, Observer { it?.let(listAdapter::submitList) })
    }

    private fun observeEvents(): Unit = with(viewModel) {
        eventExpiredToken.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    endExpireTokenProcess()
                    requireActivity().logout()
                }
            }
        })
    }

}