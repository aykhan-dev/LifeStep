package az.rabita.lifestep.ui.fragment.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentLoginBinding
import az.rabita.lifestep.ui.activity.forgotPassword.ForgotPasswordActivity
import az.rabita.lifestep.ui.activity.main.MainActivity
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.*
import az.rabita.lifestep.viewModel.activity.auth.AuthViewModel
import az.rabita.lifestep.viewModel.fragment.login.LoginViewModel

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    private val authViewModel: AuthViewModel by activityViewModels()
    private val viewModel: LoginViewModel by activityViewModels()

    private val navController by lazy { findNavController() }

    private val loadingDialog by lazy { LoadingDialog() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater)

        binding.apply {
            lifecycleOwner = this@LoginFragment
            viewModel = this@LoginFragment.viewModel
        }

        with(binding) {
            buttonForgotPassword.setOnClickListener {
                startActivity(Intent(requireActivity(), ForgotPasswordActivity::class.java))
            }
            root.setOnClickListener { root.hideKeyboard(context) }
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeStates()
        observeEvents()
    }

    private fun observeStates(): Unit = with(viewModel) {

        authViewModel.stateFabClick.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    if (
                        navController.currentDestination?.id == R.id.loginFragment &&
                        viewModel.uiState.value != UiState.Loading
                    )
                        viewModel.loginUser()
                }
            }
        })

        stateToRegisterButtonClick.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    navController.navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
                }
            }
        })

        stateInternetConnection.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) activity?.let { activity ->
                    MessageDialog(MessageType.ERROR, NO_INTERNET_CONNECTION).show(
                        activity.supportFragmentManager,
                        "No Internet"
                    )
                }
            }
        })

        uiState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    is UiState.Loading -> activity?.supportFragmentManager?.let { fm ->
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

        errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let {
                activity?.let { activity ->
                    MessageDialog(MessageType.ERROR, it).show(
                        activity.supportFragmentManager,
                        ERROR_TAG
                    )
                }
            }
        })

    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventNavigateToMainActivity.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    activity?.let { activity ->
                        startActivity(Intent(activity, MainActivity::class.java))
                        activity.finish()
                    }
                }
            }
        })

        eventExpiredToken.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    activity?.logout()
                    endExpireTokenProcess()
                }
            }
        })

    }

}
