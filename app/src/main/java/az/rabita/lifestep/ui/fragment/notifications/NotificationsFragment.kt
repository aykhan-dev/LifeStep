package az.rabita.lifestep.ui.fragment.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.databinding.FragmentNotificationsBinding
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.notifications.NotificationsViewModel


class NotificationsFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding

    private val viewModel: NotificationsViewModel by viewModels()

    private val navController by lazy { findNavController() }
    private val notificationsAdapter by lazy { NotificationRecyclerAdapter {} }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationsBinding.inflate(inflater)

        binding.apply {
            lifecycleOwner = this@NotificationsFragment
            viewModel = this@NotificationsFragment.viewModel
        }

        with(binding) {
            imageButtonBack.setOnClickListener { navController.popBackStack() }
        }

        return binding.root
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

    override fun onStart() {
        super.onStart()
        viewModel.fetchListOfNotifications()
    }

    private fun configureRecyclerView() = with(binding.recyclerViewNotifications) {
        adapter = notificationsAdapter
    }

    private fun observeData(): Unit = with(viewModel) {

        listOfNotifications.observe(viewLifecycleOwner, Observer {
            it?.let { notificationsAdapter.submitList(it) }
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
