package az.rabita.lifestep.ui.fragment.history.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import az.rabita.lifestep.databinding.FragmentPageBinding
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.history.HistoryViewModel
import kotlinx.coroutines.launch

class PageHistoryFragment(private val pageType: HistoryPageType) : Fragment() {

    private lateinit var binding: FragmentPageBinding

    private val viewModel by viewModels<HistoryViewModel>()

    private val adapter = PageHistoryRecyclerAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPageBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
        configureRecyclerView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@PageHistoryFragment
    }

    private fun configureRecyclerView(): Unit = with(binding.recyclerViewHistory) {
        adapter = this@PageHistoryFragment.adapter
    }

    private fun observeData(): Unit = with(viewModel) {

        lifecycleScope.launch {
            fetchListOfDonations(pageType).observe(viewLifecycleOwner, Observer {
                adapter.submitData(lifecycle, it)
            })
        }

        errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let {
                activity?.let { activity ->
                    MessageDialog(it).show(
                        activity.supportFragmentManager,
                        ERROR_TAG
                    )
                }
            }
        })

    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventExpiredToken.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    activity?.logout()
                    endExpireTokenProcess()
                }
            }
        })

    }

}