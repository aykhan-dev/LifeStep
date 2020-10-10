package az.rabita.lifestep.ui.fragment.password

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentPasswordBinding
import az.rabita.lifestep.utils.hideKeyboard
import az.rabita.lifestep.viewModel.fragment.forgotPassword.ForgotPasswordViewModel
import az.rabita.lifestep.viewModel.fragment.password.PasswordViewModel


class PasswordFragment : Fragment() {

    private lateinit var binding: FragmentPasswordBinding

    private lateinit var buttonTexts: List<String>

    private val sharedViewModel by activityViewModels<ForgotPasswordViewModel>()
    private val viewModel by viewModels<PasswordViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPasswordBinding.inflate(inflater)
        buttonTexts = listOf(getString(R.string.show), getString(R.string.hide))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@PasswordFragment
        viewModel = this@PasswordFragment.sharedViewModel
        viewModelFragment = this@PasswordFragment.viewModel

        imageButtonBack.setOnClickListener { activity?.onBackPressed() }
        root.setOnClickListener { it.hideKeyboard() }
    }

    private fun observeData(): Unit = with(viewModel) {

        textOfButtonShow1.observe(viewLifecycleOwner, {
            it?.let { text ->
                with(binding) {
                    when (text) {
                        buttonTexts[0] -> editTextPassword.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                        buttonTexts[1] -> editTextPassword.transformationMethod = null
                    }
                }
            }
        })

        textOfButtonShow2.observe(viewLifecycleOwner, {
            it?.let { text ->
                with(binding) {
                    when (text) {
                        buttonTexts[0] -> editTextPasswordConfirm.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                        buttonTexts[1] -> editTextPasswordConfirm.transformationMethod = null
                    }
                }
            }
        })

    }

}
