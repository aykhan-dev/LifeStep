package az.rabita.lifestep.ui.activity.forgotPassword

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.ActivityForgotPasswordBinding
import az.rabita.lifestep.manager.LocaleManager
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.utils.*
import az.rabita.lifestep.viewModel.fragment.forgotPassword.ForgotPasswordViewModel

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private val viewModel by viewModels<ForgotPasswordViewModel>()

    private val navController by lazy { findNavController(R.id.fragment_password_host) }

    private val loadingDialog = LoadingDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password)

        bindUI()

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

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@ForgotPasswordActivity
    }

    private fun observeStates(): Unit = with(viewModel) {

        uiState.observe(this@ForgotPasswordActivity, {
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

        errorMessage.observe(this@ForgotPasswordActivity, {
            it?.let {
                MessageDialog.getInstance(it).show(supportFragmentManager, ERROR_TAG)
            }
        })

    }

    private fun navigate(): Unit = with(viewModel) {

        eventBack.observe(this@ForgotPasswordActivity, {
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
