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
import az.rabita.lifestep.ui.dialog.message.SingleMessageDialog
import az.rabita.lifestep.utils.*
import az.rabita.lifestep.viewModel.activity.auth.AuthViewModel
import az.rabita.lifestep.viewModel.fragment.login.LoginViewModel

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    private val authViewModel by activityViewModels<AuthViewModel>()
    private val viewModel by activityViewModels<LoginViewModel>()

    private val navController by lazy { findNavController() }

    private val loadingDialog = LoadingDialog()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeStates()
        observeEvents()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@LoginFragment
        viewModel = this@LoginFragment.viewModel

        buttonForgotPassword.setOnClickListener {
            startActivity(Intent(requireActivity(), ForgotPasswordActivity::class.java))
        }
        root.setOnClickListener { root.hideKeyboard(context) }
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
            it?.let { errorMsg ->
                activity?.let { activity ->
                    SingleMessageDialog.popUp(
                        activity.supportFragmentManager,
                        ERROR_TAG,
                        errorMsg
                    )
                }
            }
        })

    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventNavigateToMainActivity.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
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
