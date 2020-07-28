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
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.hideKeyboard
import az.rabita.lifestep.viewModel.fragment.forgotPassword.ForgotPasswordViewModel

class PinFragment : Fragment() {

    private lateinit var binding: FragmentPinBinding

    private val viewModel: ForgotPasswordViewModel by activityViewModels()

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPinBinding.inflate(inflater)

        binding.apply {
            lifecycleOwner = this@PinFragment
            viewModel = this@PinFragment.viewModel
        }

        with(binding) {
            imageButtonBack.setOnClickListener { navController.popBackStack() }
            root.setOnClickListener { it.hideKeyboard(context) }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurations()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
    }

    private fun configurations() = with(binding) {

        pinView.addTextChangeMethod {
            this@PinFragment.viewModel.pinInput.postValue(it)
        }

    }

    private fun observeData(): Unit = with(viewModel) {

        errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let {
                activity?.let { activity ->
                    MessageDialog(it).show(
                        activity.supportFragmentManager,
                        ERROR_TAG
                    )
                }
            }
        })

    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventNavigateToPasswordFragment.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) navController.navigate(PinFragmentDirections.actionPinFragmentToPasswordFragment())
            }
        })

    }

}
