package az.rabita.lifestep.ui.fragment.searchResults

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import az.rabita.lifestep.databinding.FragmentSearchResultBinding
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.LOADING_TAG
import az.rabita.lifestep.utils.UiState
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.searchResults.SearchResultsViewModel

class SearchResultsFragment : Fragment() {

    private lateinit var binding: FragmentSearchResultBinding

    private val viewModel: SearchResultsViewModel by viewModels()
    private val args: SearchResultsFragmentArgs by navArgs()

    private val navController by lazy { findNavController() }

    private val adapter = LargeSearchResultsRecyclerAdapter { userId ->
        navController.navigate(
            SearchResultsFragmentDirections.actionSearchResultsFragmentToUserProfileFragment(
                userId
            )
        )
    }

    private val loadingDialog by lazy { LoadingDialog() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchResultBinding.inflate(inflater)
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

    override fun onResume() {
        super.onResume()
        viewModel.fetchSearchResults(args.searchKeyword)
    }

    private fun bindUI() = with(binding) {
        imageButtonBack.setOnClickListener { activity?.onBackPressed() }
    }

    private fun configureRecyclerView(): Unit = with(binding) {
        recyclerViewResults.adapter = this@SearchResultsFragment.adapter
    }

    private fun observeData(): Unit = with(viewModel) {
        listOfSearchResult.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it.isNotEmpty()) adapter.submitList(it)
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

    private fun observeStates(): Unit = with(viewModel) {
        searchingState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    is UiState.Loading -> {
                        activity?.let { activity ->
                            loadingDialog.show(activity.supportFragmentManager, LOADING_TAG)
                        }
                    }
                    is UiState.LoadingFinished -> loadingDialog.dismiss()
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