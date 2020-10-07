package az.rabita.lifestep.ui.fragment.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.databinding.FragmentEventsBinding
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.events.EventsViewModel

class EventsFragment : Fragment() {

    private lateinit var binding: FragmentEventsBinding

    private val viewModel by viewModels<EventsViewModel>()

    private val navController by lazy { findNavController() }

    private val adapter = EventsRecyclerAdapter { id -> navigateTo(id) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventsBinding.inflate(inflater)
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

    override fun onStart() {
        super.onStart()
        viewModel.fetchAllCategories()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@EventsFragment
        viewModel = this@EventsFragment.viewModel
    }

    private fun configureRecyclerView() = with(binding.recyclerViewEvents) {
        adapter = this@EventsFragment.adapter
    }

    private fun navigateTo(eventId: Int): Unit = when (eventId) {
        1 -> navController.navigate(EventsFragmentDirections.actionEventsFragmentToDonationFragment())
        else -> run { }
    }

    private fun observeData(): Unit = with(viewModel) {

        listOfCategories.observe(viewLifecycleOwner, {
            it?.let { adapter.submitList(it) }
        })

        errorMessage.observe(viewLifecycleOwner, {
            it?.let { errorMsg ->
                MessageDialog.getInstance(errorMsg).show(
                    requireActivity().supportFragmentManager,
                    ERROR_TAG
                )
            }
        })

    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventExpiredToken.observe(viewLifecycleOwner, {
            it?.let {
                if (it) {
                    activity?.logout()
                    endExpireTokenProcess()
                }
            }
        })

    }

}
