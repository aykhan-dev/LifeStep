package az.rabita.lifestep.ui.fragment.register

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
import az.rabita.lifestep.databinding.FragmentRegisterBinding
import az.rabita.lifestep.ui.activity.main.MainActivity
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.LOADING_TAG
import az.rabita.lifestep.utils.UiState
import az.rabita.lifestep.utils.hideKeyboard
import az.rabita.lifestep.viewModel.activity.auth.AuthViewModel
import az.rabita.lifestep.viewModel.fragment.register.RegistrationViewModel

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var pagerAdapter: RegisterPagerAdapter

    private val authViewModel by activityViewModels<AuthViewModel>()
    private val viewModel by activityViewModels<RegistrationViewModel>()

    private val navController by lazy { findNavController() }

    private val loadingDialog = LoadingDialog()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater)
        pagerAdapter = RegisterPagerAdapter({ swipeBackOnViewPager() }, requireActivity())
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
        lifecycleOwner = this@RegisterFragment
        viewModel = this@RegisterFragment.viewModel
        pagerAdapter = this@RegisterFragment.pagerAdapter

        root.setOnClickListener { root.hideKeyboard() }
    }

    private fun swipeBackOnViewPager(): Unit = with(binding) {
        viewPager.currentItem = 0
    }

    private fun observeStates(): Unit = with(viewModel) {

        authViewModel.stateFabClick.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    if (
                        navController.currentDestination?.id == R.id.registerFragment &&
                        viewModel.uiState.value != UiState.Loading
                    )
                        if (binding.viewPager.currentItem == 0) viewModel.firstRegistrationPartChecking()
                        else viewModel.secondRegistrationPartChecking()
                }
            }
        })

        uiState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    is UiState.Loading -> loadingDialog.show(
    requireActivity().supportFragmentManager,
    ERROR_TAG
)
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
                MessageDialog.getInstance(errorMsg).show(
                    requireActivity().supportFragmentManager,
                    ERROR_TAG
                )
            }
        })

    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventNavigateToMainActivity.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    val activity = requireActivity()
                    startActivity(Intent(activity, MainActivity::class.java))
                    activity.finish()
                }
            }
        })

        eventNavigateToNextRegisterFragment.observe(viewLifecycleOwner, Observer {
            it?.let { if (it) binding.viewPager.currentItem = 1 }
        })

    }

}
