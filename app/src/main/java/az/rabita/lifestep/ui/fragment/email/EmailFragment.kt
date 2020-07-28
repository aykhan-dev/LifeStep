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
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.hideKeyboard
import az.rabita.lifestep.viewModel.fragment.forgotPassword.ForgotPasswordViewModel

class EmailFragment : Fragment() {

    private lateinit var binding: FragmentEmailBinding

    private val viewModel: ForgotPasswordViewModel by activityViewModels()

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmailBinding.inflate(inflater)

        binding.apply {
            lifecycleOwner = this@EmailFragment
            viewModel = this@EmailFragment.viewModel
        }

        with(binding) {
            imageButtonBack.setOnClickListener { activity?.finish() }
            root.setOnClickListener { it.hideKeyboard(context) }
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
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

        eventNavigateToPinFragment.observe(viewLifecycleOwner, Observer {
            it?.let { if (it) navController.navigate(EmailFragmentDirections.actionEmailFragmentToPinFragment()) }
        })

    }

}
