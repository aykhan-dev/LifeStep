package az.rabita.lifestep.ui.activity.forgotPassword

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.ActivityForgotPasswordBinding
import az.rabita.lifestep.manager.LocaleManager
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.DEFAULT_LANG_KEY
import az.rabita.lifestep.utils.LOADING_TAG
import az.rabita.lifestep.utils.MAIN_TO_FORGOT_PASSWORD_KEY
import az.rabita.lifestep.utils.UiState
import az.rabita.lifestep.viewModel.fragment.forgotPassword.ForgotPasswordViewModel

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private lateinit var viewModel: ForgotPasswordViewModel

    private val navController by lazy { findNavController(R.id.fragment_password_host) }
    private val loadingDialog by lazy { LoadingDialog() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password)

        viewModel = ViewModelProvider(this).get(ForgotPasswordViewModel::class.java)

        binding.apply {
            lifecycleOwner = this@ForgotPasswordActivity
        }

        viewModel.changeFromEditProfilePage(
            intent.getBooleanExtra(
                MAIN_TO_FORGOT_PASSWORD_KEY,
                false
            )
        )

        if (viewModel.fromEditProfilePage) navController.navigate(R.id.passwordFragment)

        observeStates()
        observeData()
        navigate()
    }

    private fun observeStates(): Unit = with(viewModel) {

        uiState.observe(this@ForgotPasswordActivity, Observer {
            it?.let {
                when (it) {
                    is UiState.Loading -> supportFragmentManager.let { fm ->
                        loadingDialog.show(
                            fm,
                            LOADING_TAG
                        )
                    }
                    is UiState.LoadingFinished -> {
                        loadingDialog.dismiss()
                        uiState.value = null
                    }
                }
            }
        })

    }

    private fun observeData(): Unit = with(viewModel) {

        errorMessage.observe(this@ForgotPasswordActivity, Observer {
            it?.let {
                MessageDialog(MessageType.ERROR, it).show(supportFragmentManager, "Error")
            }
        })

    }

    private fun navigate() = with(viewModel) {

        eventBack.observe(this@ForgotPasswordActivity, Observer {
            it?.let { if (it) finish() }
        })

    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.onAttach(newBase, DEFAULT_LANG_KEY))
    }

    override fun onBackPressed() {
        if (viewModel.fromEditProfilePage) finish()
        else super.onBackPressed()
    }

}
