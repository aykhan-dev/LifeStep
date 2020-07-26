package az.rabita.lifestep.ui.fragment.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentFriendsBinding
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.friends.FriendsViewModel
import com.google.android.material.tabs.TabLayoutMediator

class FriendsFragment : Fragment() {

    private lateinit var binding: FragmentFriendsBinding

    private val viewModel: FriendsViewModel by viewModels()

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendsBinding.inflate(inflater)

        binding.apply {
            lifecycleOwner = this@FriendsFragment
        }

        with(binding) {
            imageButtonBack.setOnClickListener { navController.popBackStack() }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchFriendshipStats()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureViewPager()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
    }

    private fun observeData(): Unit = with(viewModel) {

        friendshipStats.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.tabLayoutFriends.getTabAt(0)?.text =
                    getString(R.string.my_friends, it.friendsCount.toString())
                binding.tabLayoutFriends.getTabAt(1)?.text =
                    getString(R.string.friend_requests, it.requestCount.toString())
            }
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

    private fun configureViewPager(): Unit = with(binding) {
        activity?.let {
            viewPager.adapter = FriendsPagerAdapter(it) { userId ->
                navController.navigate(
                    FriendsFragmentDirections.actionFriendsFragmentToUserProfileFragment(userId)
                )
            }
        }
        TabLayoutMediator(tabLayoutFriends, viewPager) { _, _ -> }.attach()
    }

}
