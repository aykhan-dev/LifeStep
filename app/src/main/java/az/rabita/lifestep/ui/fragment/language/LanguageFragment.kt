package az.rabita.lifestep.ui.fragment.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.databinding.FragmentLanguageBinding
import az.rabita.lifestep.manager.LocaleManager
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.utils.*
import az.rabita.lifestep.viewModel.fragment.language.LanguageViewModel
import kotlinx.android.synthetic.main.fragment_language.*

class LanguageFragment : Fragment() {

    private lateinit var binding: FragmentLanguageBinding
    private lateinit var sharedPreferences: PreferenceManager

    private val viewModel by viewModels<LanguageViewModel>()
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLanguageBinding.inflate(inflater)
        sharedPreferences = PreferenceManager.getInstance(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
        configureListeners()
        when (sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)) {
            LANG_AZ -> switcher_az.isSwitchOn = true
            LANG_EN -> switcher_en.isSwitchOn = true
            LANG_RU -> switcher_ru.isSwitchOn = true
            0 -> activity?.finish()
        }
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@LanguageFragment
        viewModel = this@LanguageFragment.viewModel

        imageButtonBack.setOnClickListener { navController.popBackStack() }
    }

    private fun configureListeners(): Unit = with(binding) {

        switcherAz.setOnSwitchListener {
            if (!it) {
                switcherAz.isSwitchOn = true
                return@setOnSwitchListener
            }

            selectLang(LANG_AZ, LANG_AZ_KEY)

            switcherEn.isSwitchOn = false
            switcherRu.isSwitchOn = false
        }

        switcherEn.setOnSwitchListener {
            if (!it) {
                switcherEn.isSwitchOn = true
                return@setOnSwitchListener
            }

            selectLang(LANG_EN, LANG_EN_KEY)

            switcherAz.isSwitchOn = false
            switcherRu.isSwitchOn = false
        }

        switcherRu.setOnSwitchListener {
            if (!it) {
                switcherRu.isSwitchOn = true
                return@setOnSwitchListener
            }

            selectLang(LANG_RU, LANG_RU_KEY)

            switcherAz.isSwitchOn = false
            switcherEn.isSwitchOn = false
        }

    }

    private fun selectLang(lang: Int, langCode: String) {
        sharedPreferences.setIntegerElement(LANG_KEY, lang)
        LocaleManager.setLocale(requireContext(), langCode)
        requireActivity().restart()
    }

}
