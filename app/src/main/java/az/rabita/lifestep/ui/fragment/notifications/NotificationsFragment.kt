package az.rabita.lifestep.ui.fragment.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.NavGraphMainDirections
import az.rabita.lifestep.databinding.FragmentNotificationsBinding
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.utils.openUrl
import az.rabita.lifestep.viewModel.fragment.notifications.NotificationsViewModel
import jp.wasabeef.recyclerview.animators.ScaleInAnimator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class NotificationsFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding

    private val viewModel by viewModels<NotificationsViewModel>()

    private val navController by lazy { findNavController() }
    private val notificationsAdapter by lazy {
        NotificationRecyclerAdapter { notification ->
            when (notification.usersNotificationsTypesId) {
                //20 -> navController.navigate(NotificationsFragmentDirections.actionNotificationsFragmentToInviteFriendFragment())
                //30 -> navController.navigate(NotificationsFragmentDirections.actionNotificationsFragmentToFriendsFragment())
                40 -> navController.navigate(
                    NavGraphMainDirections.actionToOtherProfileFragment(notification.usersId)
                )
                else -> if (notification.url.isNotEmpty()) openUrl(notification.url)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationsBinding.inflate(inflater)
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
        viewModel.fetchListOfNotifications()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@NotificationsFragment
        viewModel = this@NotificationsFragment.viewModel

        imageButtonBack.setOnClickListener { navController.popBackStack() }
    }

    private fun configureRecyclerView(): Unit = with(binding.recyclerViewNotifications) {
        adapter = notificationsAdapter
        itemAnimator = ScaleInAnimator().apply { addDuration = 100 }
    }

    private fun observeData(): Unit = with(viewModel) {

        lifecycleScope.launch {
            listOfNotifications.collectLatest {
                notificationsAdapter.submitList(it)
            }
        }

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
