package az.rabita.lifestep.ui.fragment.pin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.databinding.FragmentPinBinding
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.hideKeyboard
import az.rabita.lifestep.viewModel.fragment.forgotPassword.ForgotPasswordViewModel

class PinFragment : Fragment() {

    private lateinit var binding: FragmentPinBinding

    private val viewModel by activityViewModels<ForgotPasswordViewModel>()

    private val navController by lazy { findNavController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.pinGeneratedTime = System.currentTimeMillis()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPinBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
        configurations()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@PinFragment
        viewModel = this@PinFragment.viewModel

        imageButtonBack.setOnClickListener { navController.popBackStack() }
        root.setOnClickListener { it.hideKeyboard() }
    }

    private fun configurations(): Unit = with(binding) {

        pinView.addTextChangeMethod {
            this@PinFragment.viewModel.pinInput.postValue(it)
        }

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

        eventNavigateToPasswordFragment.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) navController.navigate(PinFragmentDirections.actionPinFragmentToPasswordFragment())
            }
        })

        eventNavigateBackToEmailFragment.observe(viewLifecycleOwner, Observer {
            it?.let {
                if(it) requireActivity().onBackPressed()
            }
        })

    }

}
