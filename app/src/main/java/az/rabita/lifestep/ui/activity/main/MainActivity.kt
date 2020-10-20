package az.rabita.lifestep.ui.activity.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.ActivityMainBinding
import az.rabita.lifestep.pojo.dataHolder.NotificationInfoHolder
import az.rabita.lifestep.ui.activity.BaseActivity
import az.rabita.lifestep.ui.fragment.home.HomeFragmentDirections
import az.rabita.lifestep.utils.NOTIFICATION_INFO_KEY
import az.rabita.lifestep.viewModel.activity.main.MainViewModel

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<MainViewModel>()

    private val navController by lazy { findNavController(R.id.fragment_main_host) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        bindUI()
        configurations()
        observeData()
        analyzeIntentComingFromNotificationEvent()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@MainActivity
        viewModel = this@MainActivity.viewModel
    }

    private fun analyzeIntentComingFromNotificationEvent() {
        intent?.let {
            val data = it.getParcelableExtra<NotificationInfoHolder>(NOTIFICATION_INFO_KEY)
            data?.let { navController.navigate(HomeFragmentDirections.actionHomeFragmentToNotificationsFragment()) }
        }
    }

    private fun configurations(): Unit = with(binding) {

        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnNavigationItemReselectedListener {

        }

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.homeFragment) this@MainActivity.viewModel.changePage(2)
        }

    }

    private fun observeData(): Unit = with(viewModel) {

        indexOfSelectedPage.observe(this@MainActivity, Observer {
            it?.let {
                with(binding.bottomNavigationView) {
                    selectedItemId = when (it) {
                        0 -> R.id.nav_graph_main_events
                        1 -> R.id.walletFragment
                        3 -> R.id.adsFragment
                        4 -> R.id.nav_graph_main_settings
                        else -> R.id.homeFragment
                    }
                }
            }
        })

    }

}
