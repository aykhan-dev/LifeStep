package az.rabita.lifestep.ui.activity.main

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.ActivityMainBinding
import az.rabita.lifestep.manager.LocaleManager
import az.rabita.lifestep.pojo.dataHolder.NotificationInfoHolder
import az.rabita.lifestep.ui.fragment.home.HomeFragmentDirections
import az.rabita.lifestep.utils.DEFAULT_LANG_KEY
import az.rabita.lifestep.utils.NOTIFICATION_INFO_KEY
import az.rabita.lifestep.utils.makeEdgeToEdge
import az.rabita.lifestep.viewModel.activity.main.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<MainViewModel>()

    private val navController by lazy { findNavController(R.id.fragment_main_host) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        makeEdgeToEdge()

        bindUI()
        analyzeIntentComingFromNotificationEvent()
        configurations()
        observeData()
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

        bottomNavigationView.setupWithNavController(navController)

        //TODO refactor item selection

        bottomNavigationView.setOnNavigationItemReselectedListener {

        }

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if(destination.id == R.id.homeFragment) this@MainActivity.viewModel.changePage(2)
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

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.onAttach(newBase, DEFAULT_LANG_KEY))
    }

}
