package az.rabita.lifestep.ui.fragment.email

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.databinding.FragmentEmailBinding
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.SingleMessageDialog
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.hideKeyboard
import az.rabita.lifestep.viewModel.fragment.forgotPassword.ForgotPasswordViewModel

class EmailFragment : Fragment() {

    private lateinit var binding: FragmentEmailBinding

    private val viewModel by activityViewModels<ForgotPasswordViewModel>()

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@EmailFragment
        viewModel = this@EmailFragment.viewModel

        imageButtonBack.setOnClickListener { activity?.finish() }
        root.setOnClickListener { it.hideKeyboard(context) }
    }

    private fun observeData(): Unit = with(viewModel) {

        errorMessage.observe(viewLifecycleOwner, {
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

        eventNavigateToPinFragment.observe(viewLifecycleOwner, {
            it?.let { if (it) navController.navigate(EmailFragmentDirections.actionEmailFragmentToPinFragment()) }
        })

    }

}
