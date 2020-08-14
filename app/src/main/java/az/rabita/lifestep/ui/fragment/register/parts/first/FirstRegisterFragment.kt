package az.rabita.lifestep.ui.fragment.register.parts.first

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import az.rabita.lifestep.databinding.FragmentRegisterOneBinding
import az.rabita.lifestep.utils.hideKeyboard
import az.rabita.lifestep.viewModel.fragment.register.RegistrationViewModel

class FirstRegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterOneBinding
    private val viewModel by activityViewModels<RegistrationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterOneBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@FirstRegisterFragment
        viewModel = this@FirstRegisterFragment.viewModel

        buttonHaveAccount.setOnClickListener { activity?.onBackPressed() }
        root.setOnClickListener { root.hideKeyboard(context) }
    }

}
