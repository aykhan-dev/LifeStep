package az.rabita.lifestep.ui.activity.auth

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.ActivityAuthBinding
import az.rabita.lifestep.manager.LocaleManager
import az.rabita.lifestep.utils.DEFAULT_LANG_KEY
import az.rabita.lifestep.utils.hideKeyboard
import az.rabita.lifestep.viewModel.activity.auth.AuthViewModel
import az.rabita.lifestep.viewModel.fragment.login.LoginViewModel
import az.rabita.lifestep.viewModel.fragment.register.RegistrationViewModel

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    private lateinit var authViewModel: AuthViewModel
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var registerViewModel: RegistrationViewModel

    private lateinit var motionLayout: MotionLayout

    private val navController by lazy { findNavController(R.id.fragment_auth_host) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth)

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        registerViewModel = ViewModelProvider(this).get(RegistrationViewModel::class.java)

        binding.apply {
            lifecycleOwner = this@AuthActivity
            viewModel = this@AuthActivity.authViewModel
        }

        with(binding) {
            this@AuthActivity.motionLayout = motionLayout
            root.setOnClickListener { root.hideKeyboard(applicationContext) }
        }

        observeClicks()
    }

    private fun observeClicks() = with(loginViewModel) {

        stateToRegisterButtonClick.observe(this@AuthActivity, Observer {
            it?.let { if (it) motionLayout.transitionToEnd() }
        })

    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.onAttach(newBase, DEFAULT_LANG_KEY))
    }

    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.registerFragment) motionLayout.transitionToStart()
        super.onBackPressed()
    }

}
