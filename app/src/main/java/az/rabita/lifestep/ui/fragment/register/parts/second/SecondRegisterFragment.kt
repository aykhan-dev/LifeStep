package az.rabita.lifestep.ui.fragment.register.parts.second

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentRegisterTwoBinding
import az.rabita.lifestep.utils.hideKeyboard
import az.rabita.lifestep.viewModel.fragment.register.RegistrationViewModel


class SecondRegisterFragment(private val buttonBackClickListener: () -> Unit) : Fragment() {

    private lateinit var binding: FragmentRegisterTwoBinding
    private val viewModel by activityViewModels<RegistrationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterTwoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
        configurations()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@SecondRegisterFragment
        viewModel = this@SecondRegisterFragment.viewModel

        buttonBack.setOnClickListener { buttonBackClickListener() }

        context?.resources?.getStringArray(R.array.genders)?.let {
            val adapter = ArrayAdapter(requireContext(), R.layout.item_gender, it)
            editTextGender.setAdapter(adapter)
        }

        editTextGender.onFocusChangeListener =
            OnFocusChangeListener { _, hasFocus -> if (hasFocus) editTextGender.showDropDown() }

        root.setOnClickListener { root.hideKeyboard() }
    }

    private fun configurations() {
        binding.editTextGender.setOnItemClickListener { _, _, position, _ ->
            viewModel.genderInput.value = position * 10 + 10
        }
    }

}
