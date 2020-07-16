package az.rabita.lifestep.ui.fragment.friends.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import az.rabita.lifestep.databinding.FragmentPageFriendsBinding
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.ui.fragment.friends.FriendsPageType
import az.rabita.lifestep.utils.*
import az.rabita.lifestep.viewModel.fragment.friends.FriendsViewModel

class PageFriendsFragment(private val pageType: FriendsPageType) : Fragment() {

    private lateinit var binding: FragmentPageFriendsBinding

    private val viewModel: FriendsViewModel by viewModels()

    private val adapter by lazy {
        PageFriendsRecyclerAdapter(pageType) { id: String, isAccepted: Boolean?, position: Int ->
            onItemClick(id, isAccepted, position)
        }
    }

    private val loadingDialog by lazy { LoadingDialog() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPageFriendsBinding.inflate(inflater)

        binding.apply {
            lifecycleOwner = this@PageFriendsFragment
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
        observeStates()
        observeEvents()
    }

    private fun configureRecyclerView() = with(binding) {
        recyclerViewFriends.adapter = adapter

        context?.let {
            recyclerViewFriends.addItemDecoration(VerticalSpaceItemDecoration(0, pxFromDp(it, 90f)))
        }
    }

    private fun observeData(): Unit = with(viewModel) {

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

        eventRefreshList.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    when (pageType) {
                        FriendsPageType.MY_FRIENDS -> {
                            fetchFriendsList().observe(
                                viewLifecycleOwner,
                                Observer { pagingData ->
                                    pagingData?.let { adapter.submitData(lifecycle, pagingData) }
                                })
                        }
                        FriendsPageType.FRIEND_REQUESTS -> {
                            fetchFriendRequestsList().observe(
                                viewLifecycleOwner,
                                Observer { pagingData ->
                                    pagingData?.let { adapter.submitData(lifecycle, pagingData) }
                                })
                        }
                    }
                }
            }
        })

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
            adapter.notifyItemRemoved(position)
        } else {
            //TODO open user page
        }
    }

}