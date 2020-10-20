package az.rabita.lifestep.ui.fragment.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.databinding.FragmentLanguageBinding
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.utils.restart
import az.rabita.lifestep.viewModel.fragment.language.LanguageViewModel
import com.yariksoffice.lingver.Lingver
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
        when (sharedPreferences.langCode) {
            PreferenceManager.LANG_CODE_AZ -> switcher_az.isSwitchOn = true
            PreferenceManager.LANG_CODE_EN -> switcher_en.isSwitchOn = true
            PreferenceManager.LANG_CODE_RU -> switcher_ru.isSwitchOn = true
        }
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@LanguageFragment
        viewModel = this@LanguageFragment.viewModel

        imageButtonBack.setOnClickListener { navController.popBackStack() }
    }

    private fun configureListeners(): Unit = with(binding) {

        switcherAz.setOnSwitchListener {
            if (!it) switcherAz.isSwitchOn = true
            else {
                switcherEn.isSwitchOn = false
                switcherRu.isSwitchOn = false
                changeLanguage(PreferenceManager.LANG_CODE_AZ)
            }
        }

        switcherEn.setOnSwitchListener {
            if (!it) switcherEn.isSwitchOn = true
            else {
                switcherAz.isSwitchOn = false
                switcherRu.isSwitchOn = false
                changeLanguage(PreferenceManager.LANG_CODE_EN)
            }
        }

        switcherRu.setOnSwitchListener {
            if (!it) switcherRu.isSwitchOn = true
            else {
                switcherAz.isSwitchOn = false
                switcherEn.isSwitchOn = false
                changeLanguage(PreferenceManager.LANG_CODE_RU)
            }
        }

    }

    private fun changeLanguage(langCode: Int) {
        sharedPreferences.langCode = langCode
        Lingver.getInstance().setLocale(requireContext(), sharedPreferences.language)
        requireActivity().restart()
    }

}
