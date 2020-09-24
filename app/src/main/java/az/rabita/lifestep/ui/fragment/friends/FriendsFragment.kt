package az.rabita.lifestep.ui.fragment.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.NavGraphMainDirections
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentFriendsBinding
import az.rabita.lifestep.ui.dialog.message.SingleMessageDialog
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.friends.FriendsViewModel
import com.google.android.material.tabs.TabLayoutMediator

class FriendsFragment : Fragment() {

    private lateinit var binding: FragmentFriendsBinding

    private val viewModel by viewModels<FriendsViewModel>()

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
        configureViewPager()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchFriendshipStats()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@FriendsFragment

        imageButtonBack.setOnClickListener { navController.popBackStack() }
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
            it?.let { errorMsg ->
                activity?.let { activity ->
                    SingleMessageDialog.popUp(
                        activity.supportFragmentManager,
                        ERROR_TAG,
                        errorMsg
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
                    NavGraphMainDirections.actionToOtherProfileFragment(userId)
                )
            }
        }
        TabLayoutMediator(tabLayoutFriends, viewPager) { _, _ -> }.attach()
    }

}
