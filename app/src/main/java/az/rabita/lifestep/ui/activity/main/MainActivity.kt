package az.rabita.lifestep.ui.activity.main

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.ActivityMainBinding
import az.rabita.lifestep.manager.LocaleManager
import az.rabita.lifestep.pojo.dataHolder.NotificationInfoHolder
import az.rabita.lifestep.ui.fragment.home.HomeFragmentDirections
import az.rabita.lifestep.utils.DEFAULT_LANG_KEY
import az.rabita.lifestep.utils.NOTIFICATION_INFO_KEY
import az.rabita.lifestep.utils.makeEdgeToEdge
import az.rabita.lifestep.viewModel.activity.main.MainViewModel
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<MainViewModel>()

    private val navController by lazy { findNavController(R.id.fragment_main_host) }

    private val builder = NavOptions.Builder().setLaunchSingleTop(true)

    private val ids = listOf(
        R.id.nav_graph_main_events,
        R.id.walletFragment,
        R.id.homeFragment,
        R.id.adsFragment,
        R.id.nav_graph_main_settings
    )

    private val destinations = listOf(
        R.id.eventsFragment,
        R.id.walletFragment,
        R.id.homeFragment,
        R.id.adsFragment,
        R.id.settingsFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        makeEdgeToEdge()

        bindUI()
        analyzeIntentComingFromNotificationEvent()
        navigate()
        configurations()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@MainActivity
        viewModel = this@MainActivity.viewModel

        ViewCompat.setOnApplyWindowInsetsListener(customCurvedLayout) { view, insets ->
            view.updatePadding(bottom = view.paddingBottom + insets.stableInsets.bottom)
            return@setOnApplyWindowInsetsListener insets
        }
    }

    private fun analyzeIntentComingFromNotificationEvent() {
        intent?.let {
            val data = it.getParcelableExtra<NotificationInfoHolder>(NOTIFICATION_INFO_KEY)
            data?.let {
                navController.navigate(HomeFragmentDirections.actionHomeFragmentToNotificationsFragment())
            }
        }
    }

    private fun configurations(): Unit = with(binding) {

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.homeFragment) this@MainActivity.viewModel.changePage(2)
        }

    }

    private fun navigate(): Unit = with(viewModel) {

        indexOfSelectedPage.observe(this@MainActivity, {
            it?.let { index ->
                if (navController.currentDestination?.id != destinations[index]) {
                    if (index != 2) {
                        builder.setPopUpTo(ids[2], false)

                        val options = builder.build()

                        try {
                            navController.navigate(ids[index], null, options)
                        } catch (e: IllegalArgumentException) {

                        }
                    } else navController.popBackStack(ids[2], false)
                } else Timber.i("Reselection same page")
            }
        })

    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.onAttach(newBase, DEFAULT_LANG_KEY))
    }

}
