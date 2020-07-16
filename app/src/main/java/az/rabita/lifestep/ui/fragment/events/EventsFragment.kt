package az.rabita.lifestep.ui.fragment.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.databinding.FragmentEventsBinding
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.VerticalSpaceItemDecoration
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.utils.pxFromDp
import az.rabita.lifestep.viewModel.fragment.events.EventsViewModel

class EventsFragment : Fragment() {

    private lateinit var binding: FragmentEventsBinding

    private val viewModel: EventsViewModel by viewModels()

    private val adapter by lazy { EventsRecyclerAdapter { id -> navigateTo(id) } }
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventsBinding.inflate(inflater)

        binding.apply {
            lifecycleOwner = this@EventsFragment
            viewModel = this@EventsFragment.viewModel
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchAllCategories()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureRecyclerView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
    }

    private fun configureRecyclerView() = with(binding.recyclerViewEvents) {
        adapter = this@EventsFragment.adapter

        activity?.let {
            addItemDecoration(
                VerticalSpaceItemDecoration(pxFromDp(it, 20f), pxFromDp(it, 90f))
            )
        }

    }

    private fun navigateTo(eventId: Int) = when (eventId) {
        1 -> navController.navigate(EventsFragmentDirections.actionEventsFragmentToDonationFragment())
        else -> run { }
    }

    private fun observeData(): Unit = with(viewModel) {

        listOfCategories.observe(viewLifecycleOwner, Observer {
            it?.let { adapter.submitList(it) }
        })

        errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let {
                activity?.let { activity ->
                    MessageDialog(MessageType.ERROR, it).show(
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
