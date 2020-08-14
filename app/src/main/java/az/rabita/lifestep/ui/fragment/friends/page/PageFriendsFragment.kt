package az.rabita.lifestep.ui.fragment.friends.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import az.rabita.lifestep.databinding.FragmentPageFriendsBinding
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.fragment.friends.FriendsPageType
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.LOADING_TAG
import az.rabita.lifestep.utils.UiState
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.friends.FriendsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PageFriendsFragment(
    private val pageType: FriendsPageType,
    private val itemClickListener: (id: String) -> Unit
) : Fragment() {

    private lateinit var binding: FragmentPageFriendsBinding

    private val viewModel by viewModels<FriendsViewModel>()

    private val adapter =
        PageFriendsRecyclerAdapter(pageType) { id: String, isAccepted: Boolean?, position: Int ->
            onItemClick(id, isAccepted, position)
        }

    private val loadingDialog = LoadingDialog()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPageFriendsBinding.inflate(inflater)
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
        observeStates()
        observeEvents()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@PageFriendsFragment
    }

    private fun configureRecyclerView(): Unit = with(binding.recyclerViewFriends) {
        adapter = this@PageFriendsFragment.adapter
    }

    private fun observeData(): Unit = with(viewModel) {

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

        when (pageType) {
            FriendsPageType.MY_FRIENDS -> {
                lifecycleScope.launch {
                    friendsListFlow.collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
                }
            }
            FriendsPageType.FRIEND_REQUESTS -> {
                lifecycleScope.launch {
                    friendsRequestListFlow.collectLatest { pagingData ->
                        adapter.submitData(
                            lifecycle,
                            pagingData
                        )
                    }
                }
            }
        }

    }

    private fun observeStates(): Unit = with(viewModel) {

        uiState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    is UiState.Loading -> activity?.supportFragmentManager?.let { fm ->
                        loadingDialog.show(
                            fm,
                            LOADING_TAG
                        )
                    }
                    is UiState.LoadingFinished -> {
                        loadingDialog.dismiss()
                        uiState.value = null
                    }
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

    private fun onItemClick(userId: String, isAccepted: Boolean?, position: Int) {
        if (isAccepted != null) {
            viewModel.processFriendshipRequest(userId, isAccepted)
            adapter.refresh()
        } else {
            itemClickListener(userId)
        }
    }

}