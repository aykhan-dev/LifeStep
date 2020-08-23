package az.rabita.lifestep.ui.fragment.home

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentHomeBinding
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.utils.*
import az.rabita.lifestep.viewModel.fragment.home.HomeViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val viewModel by viewModels<HomeViewModel>()

    private val navController by lazy { findNavController() }

    private val adapter = SearchResultRecyclerAdapter { onSearchResultItemClick(it) }
    private val loadingDialog = LoadingDialog()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)
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

    override fun onStart() {
        super.onStart()
        lifecycleScope.launchWhenStarted { permissions() }
        viewModel.fetchWeeklyStats()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@HomeFragment
        viewModel = this@HomeFragment.viewModel

        imageButtonNotification.setOnClickListener {
            hideSearchBar()
            navigateToNotificationsPage()
        }

        imageButtonSearch.setOnClickListener { showSearchBar() }

        textViewMore.setOnClickListener {
            navController.navigate(
                HomeFragmentDirections.actionHomeFragmentToSearchResultsFragment(
                    this@HomeFragment.viewModel.searchInput.value ?: ""
                )
            )
        }

        buttonConvertSteps.setOnClickListener {
            activity?.let { loadingDialog.show(it.supportFragmentManager, "Loading") }
            this@HomeFragment.viewModel.createAdsTransaction()
        }

        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab): Unit = onWeekDaySelect(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        scrollableContent.setOnClickListener { hideSearchBar() }
    }

    private fun onWeekDaySelect(position: Int) {
        lifecycleScope.launch {
            viewModel.weeklyStats.value?.let { list ->
                if (list.isNotEmpty()) {
                    with(binding) {
                        with(list[position]) {
                            with(requireContext()) {
                                textViewCount.text = shortenString(stepCount, 6)
                                textViewConvertedCount.text = shortenString(convertedSteps)
                                textViewUnconvertedCount.text = shortenString(unconvertedSteps)
                            }
                            textViewDistanceCount.text = kilometers.toString()
                            textViewCalorieCount.text = calories.toString()
                            configureSeekBar(convertedSteps, stepCount)
                        }
                    }
                }
            }
        }
    }

    private fun configureRecyclerView(): Unit = with(binding) {
        recyclerViewResults.adapter = adapter

        context?.let {
            recyclerViewResults.addItemDecoration(
                VerticalSpaceItemDecoration(pxFromDp(it, 20f), 0)
            )
        }
    }

    private fun configureSeekBar(converted: Long, totalSteps: Long): Unit = with(binding) {
        ValueAnimator.ofInt(
            seekBar.progressDisplay,
            if (totalSteps != 0L) ((converted / totalSteps.toFloat()) * 100).toInt() else 0
        ).apply {
            duration = 300L
            repeatCount = 0
            addUpdateListener {
                seekBar.setProgressDisplayAndInvalidate(it.animatedValue as Int)
            }
        }.start()
    }

    private fun observeStates(): Unit = with(viewModel) {

        searchingState.observe(viewLifecycleOwner, Observer {
            it?.let {
                val flag = it is UiState.Loading
                binding.recyclerViewResults.isVisible = !flag
                binding.progressBar.isVisible = flag
            }
        })

    }

    private fun observeData(): Unit = with(viewModel) {

        adsTransaction.observe(viewLifecycleOwner, Observer {
            it?.let {
                loadingDialog.dismiss()
                val transactionInfo = it.asAdsTransactionInfoHolderObject()
                navController.navigate(
                    HomeFragmentDirections.actionHomeFragmentToAdsDialogFragment(
                        transactionInfo
                    )
                )
            }
        })

        weeklyStats.observe(viewLifecycleOwner, Observer {
            it?.let { list ->
                lifecycleScope.launch {
                    if (list.isNotEmpty() && list.size == 7) {
                        with(binding) {
                            tabLayout.removeAllTabs()
                            list.forEach { value ->
                                with(tabLayout) {
                                    val tab = newTab().apply { text = value.weekName }
                                    addTab(tab, false)
                                    if (value.isSelected) selectTab(tab)
                                }
                            }
                        }
                    }
                }
            }
        })

        listOfSearchResult.observe(viewLifecycleOwner, Observer {
            it?.let {
                with(binding) {
                    with(frameLayoutSearchResults) {
                        if (it.isNotEmpty()) {
                            isVisible = editTextSearchBar.isVisible
                            if (isVisible) {
                                if (it.size > 5) binding.textViewMore.visibility = VISIBLE
                                else binding.textViewMore.visibility = GONE
                                adapter.submitList(it)
                            }
                        } else {
                            isVisible = false
                        }
                    }
                }
            }
        })

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

    private fun navigateToNotificationsPage(): Unit = with(navController) {
        navigate(HomeFragmentDirections.actionHomeFragmentToNavGraphNotifications())
    }

    private fun onSearchResultItemClick(userId: String): Unit = with(navController) {
        navigate(HomeFragmentDirections.actionHomeFragmentToUserProfileFragment(userId))
    }

    private fun showSearchBar(): Unit = with(binding) {
        if (!this@HomeFragment.viewModel.searchInput.value.isNullOrEmpty()) this@HomeFragment.viewModel.fetchSearchResults()
        editTextSearchBar.makeVisible()
        textViewTitle.makeInvisible()
    }

    private fun hideSearchBar(): Unit = with(binding) {
        editTextSearchBar.makeInvisible()
        textViewTitle.makeVisible()
        if (frameLayoutSearchResults.isVisible) frameLayoutSearchResults.visibility = GONE
        root.hideKeyboard(context)
    }

    private fun permissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                ACTIVITY_RECOGNITION_REQUEST_CODE
            )
        } else {
            googleAuthFlow()
        }

    }

    private fun googleAuthFlow() {

        val account = GoogleSignIn.getAccountForExtension(requireActivity(), FITNESS_OPTIONS)

        if (!GoogleSignIn.hasPermissions(account, FITNESS_OPTIONS)) {

            GoogleSignIn.requestPermissions(
                this,
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, // e.g. 1
                account,
                FITNESS_OPTIONS
            )

        } else viewModel.accessGoogleFit()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> {
                val result = GoogleSignIn.getSignedInAccountFromIntent(data)
                result?.let {
                    if (result.isSuccessful) {
                        Timber.i("Google fit permission accepted")
                        viewModel.accessGoogleFit()
                    } else {
                        viewModel.showMessageDialog(getString(R.string.google_auth_fail_message))
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ACTIVITY_RECOGNITION_REQUEST_CODE -> googleAuthFlow()
        }
    }

}
