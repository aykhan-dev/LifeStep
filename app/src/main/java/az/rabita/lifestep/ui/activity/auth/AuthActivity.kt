package az.rabita.lifestep.ui.activity.auth

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.ActivityAuthBinding
import az.rabita.lifestep.manager.LocaleManager
import az.rabita.lifestep.utils.DEFAULT_LANG_KEY
import az.rabita.lifestep.utils.hideKeyboard
import az.rabita.lifestep.viewModel.activity.auth.AuthViewModel
import az.rabita.lifestep.viewModel.fragment.login.LoginViewModel
import java.util.*


class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    private val authViewModel by viewModels<AuthViewModel>()
    private val loginViewModel by viewModels<LoginViewModel>()

    private val navController by lazy { findNavController(R.id.fragment_auth_host) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth)

        bindUI()
        observeStates()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@AuthActivity
        viewModel = this@AuthActivity.authViewModel

        root.setOnClickListener { root.hideKeyboard(applicationContext) }
    }

    private fun observeStates() = with(loginViewModel) {

        stateToRegisterButtonClick.observe(this@AuthActivity, {
            it?.let { if (it) binding.motionLayout.transitionToEnd() }
        })

    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.onAttach(newBase, DEFAULT_LANG_KEY))
    }

    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.registerFragment) binding.motionLayout.transitionToStart()
        super.onBackPressed()
    }

}
