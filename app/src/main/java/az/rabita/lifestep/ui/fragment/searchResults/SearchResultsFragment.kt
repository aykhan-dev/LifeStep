package az.rabita.lifestep.ui.fragment.searchResults

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import az.rabita.lifestep.NavGraphMainDirections
import az.rabita.lifestep.databinding.FragmentSearchResultBinding
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.LOADING_TAG
import az.rabita.lifestep.utils.UiState
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.searchResults.SearchResultsViewModel
import jp.wasabeef.recyclerview.animators.ScaleInAnimator

class SearchResultsFragment : Fragment() {

    private lateinit var binding: FragmentSearchResultBinding

    private val viewModel by viewModels<SearchResultsViewModel>()
    private val args by navArgs<SearchResultsFragmentArgs>()

    private val navController by lazy { findNavController() }

    private val adapter = LargeSearchResultsRecyclerAdapter { userId ->
        viewModel.cachedOwnProfileInfo.value?.let { info ->
            if (info.id == userId) navController.navigate(NavGraphMainDirections.actionToOwnProfileFragment())
            else navController.navigate(NavGraphMainDirections.actionToOtherProfileFragment(userId))
        }
    }

    private val loadingDialog = LoadingDialog()

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

    private fun bindUI(): Unit = with(binding) {
        imageButtonBack.setOnClickListener { activity?.onBackPressed() }
    }

    private fun configureRecyclerView(): Unit = with(binding) {
        recyclerViewResults.adapter = this@SearchResultsFragment.adapter
        recyclerViewResults.itemAnimator = ScaleInAnimator().apply { addDuration = 100 }
    }

    private fun observeData(): Unit = with(viewModel) {

        //DON'T REMOVE THIS LINE, ELSE IT WILL BE NULL
        cachedOwnProfileInfo.observe(viewLifecycleOwner, {})

        listOfSearchResult.observe(viewLifecycleOwner, {
            it?.let {
                if (it.isNotEmpty()) adapter.submitList(it)
            }
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

    private fun observeStates(): Unit = with(viewModel) {

        searchingState.observe(viewLifecycleOwner, {
            it?.let {
                when (it) {
                    is UiState.Loading -> loadingDialog.show(
                        requireActivity().supportFragmentManager,
                        LOADING_TAG
                    )
                    is UiState.LoadingFinished -> loadingDialog.dismiss()
                }
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