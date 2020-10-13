package az.rabita.lifestep.ui.fragment.home

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.NavGraphMainDirections
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentHomeBinding
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.ui.dialog.ads.AdsDialog
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.*
import az.rabita.lifestep.viewModel.fragment.home.HomeViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val viewModel by viewModels<HomeViewModel>()

    private val navController by lazy { findNavController() }

    private val adapter = SearchResultRecyclerAdapter { onSearchResultItemClick(it) }

    private val loadingDialog = LoadingDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch(Dispatchers.IO) {
            whenResumed {
                permissions()
            }
        }
    }

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
        retrieveAdsResults()
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchOwnProfileInfo()
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

        textViewCount.setOnClickListener {
            this@HomeFragment.viewModel.accessGoogleFit()
        }

        buttonConvertSteps.setOnClickListener {
            this@HomeFragment.viewModel.createAdsTransaction()
        }

        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab): Unit = onWeekDaySelect(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        scrollableContent.setOnClickListener { hideSearchBar() }

        editTextSearchBar.setOnKeyListener { view, actionId, event ->
            if ((event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_SEARCH)) {
                this@HomeFragment.viewModel.fetchSearchResults()
                view.hideKeyboard()
            }
            false
        }

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

        uiState.observe(viewLifecycleOwner, {
            it?.let {
                when (it) {
                    is UiState.Loading -> loadingDialog.show(
                        requireActivity().supportFragmentManager,
                        ERROR_TAG
                    )
                    is UiState.LoadingFinished -> {
                        loadingDialog.dismiss()
                        uiState.value = null
                    }
                }
            }
        })

    }

    private fun observeData(): Unit = with(viewModel) {

        //DON'T REMOVE THIS LINE, ELSE IT WILL BE NULL
        cachedOwnProfileInfo.observe(viewLifecycleOwner, {})

        adsTransaction.observe(viewLifecycleOwner, {
            it?.let {
                loadingDialog.dismiss()
                val transactionInfo = it.asAdsTransactionInfoHolderObject()
                navController.navigate(NavGraphMainDirections.actionToAdsDialog(transactionInfo))
            }
        })

        weeklyStats.observe(viewLifecycleOwner, {
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

        listOfSearchResult.observe(viewLifecycleOwner, {
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

        errorMessage.observe(viewLifecycleOwner, {
            it?.let { errorMsg ->
                MessageDialog.getInstance(errorMsg).show(
                    requireActivity().supportFragmentManager,
                    ERROR_TAG
                )
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

    private fun navigateToNotificationsPage(): Unit = with(navController) {
        navigate(HomeFragmentDirections.actionHomeFragmentToNotificationsFragment())
    }

    private fun onSearchResultItemClick(userId: String): Unit = with(navController) {
        viewModel.cachedOwnProfileInfo.value?.let {
            if (it.id == userId) navigate(NavGraphMainDirections.actionToOwnProfileFragment())
            else navigate(NavGraphMainDirections.actionToOtherProfileFragment(userId))
        }
    }

    private fun showSearchBar(): Unit = with(binding) {
        if (editTextSearchBar.isVisible) {
            this@HomeFragment.viewModel.fetchSearchResults()
        } else {
            if (!this@HomeFragment.viewModel.searchInput.value.isNullOrEmpty()) this@HomeFragment.viewModel.fetchSearchResults()
            editTextSearchBar.makeVisible()
            textViewTitle.makeInvisible()
        }
    }

    private fun hideSearchBar(): Unit = with(binding) {
        editTextSearchBar.makeInvisible()
        textViewTitle.makeVisible()
        if (frameLayoutSearchResults.isVisible) frameLayoutSearchResults.visibility = GONE
        root.hideKeyboard()
    }

    private fun retrieveAdsResults() {

        val stateHandle = navController.currentBackStackEntry!!.savedStateHandle

        stateHandle.getLiveData<Map<String, Any>?>(AdsDialog.RESULT_KEY)
            .observe(viewLifecycleOwner, Observer {
                if (viewModel.adsGenerated) {
                    //IF IT IS REMOVED, LOADING DIALOG WILL APPEAR EACH TIME WHILE OPENING HOME PAGE AFTER WATCHING ADS
                    viewModel.adsGenerated = false
                    it?.let(viewModel::convertSteps)
                }
            })

    }

    private fun permissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                ACTIVITY_RECOGNITION_REQUEST_CODE
            )
        } else {
            if (!requireContext().appIsExist("com.google.android.apps.fitness")) {
                MessageDialog.getInstance(
                    Message(
                        getString(R.string.google_auth_downlaod_message),
                        MessageType.GOOGLE_FIT_NOT_DOWNLOADED
                    )
                ).show(
                    requireActivity().supportFragmentManager,
                    ERROR_TAG
                )
            } else googleAuthFlow()
        }

    }

    private fun googleAuthFlow() {

        val account =
            GoogleSignIn.getAccountForExtension(requireActivity(), FITNESS_OPTIONS)

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
                val result =
                    GoogleSignIn.getSignedInAccountFromIntent(data).addOnFailureListener {
                        Timber.e(it)
                    }
                result.let {
                    if (result.isSuccessful) {
                        Timber.i("Google fit permission accepted")
                        viewModel.accessGoogleFit()
                    } else {
                        viewModel.showMessageDialogSync(
                            getString(R.string.google_auth_fail_message),
                            MessageType.GOOGLE_FIT_NOT_CONNECTED
                        )
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
