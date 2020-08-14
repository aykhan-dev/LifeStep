package az.rabita.lifestep.ui.activity.main

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.ActivityMainBinding
import az.rabita.lifestep.manager.LocaleManager
import az.rabita.lifestep.pojo.dataHolder.NotificationInfoHolder
import az.rabita.lifestep.ui.fragment.home.HomeFragmentDirections
import az.rabita.lifestep.utils.DEFAULT_LANG_KEY
import az.rabita.lifestep.utils.NOTIFICATION_INFO_KEY
import az.rabita.lifestep.viewModel.activity.main.MainViewModel
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<MainViewModel>()

    private val navController by lazy { findNavController(R.id.fragment_main_host) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        bindUI()
        analyzeIntentComingFromNotificationEvent()
        navigate()
        configurations()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@MainActivity
        viewModel = this@MainActivity.viewModel
    }

    private fun analyzeIntentComingFromNotificationEvent() {
        intent?.let {
            val data = it.getParcelableExtra<NotificationInfoHolder>(NOTIFICATION_INFO_KEY)
            data?.let {
                navController.navigate(HomeFragmentDirections.actionHomeFragmentToNavGraphNotifications())
            }
        }
    }

    private fun configurations(): Unit = with(binding) {

        customCurvedLayout.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.homeFragment) this@MainActivity.viewModel.changePage(2)
        }

    }

    private fun navigate(): Unit = with(viewModel) {

        indexOfSelectedPage.observe(this@MainActivity, Observer {
            it?.let { binding.customCurvedLayout.navigate(it) }
        })

    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.onAttach(newBase, DEFAULT_LANG_KEY))
    }

}
