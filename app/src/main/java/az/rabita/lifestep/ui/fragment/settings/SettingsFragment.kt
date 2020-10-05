package az.rabita.lifestep.ui.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.NavGraphMainDirections
import az.rabita.lifestep.databinding.FragmentSettingsBinding
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.SingleMessageDialog
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.LOADING_TAG
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.settings.SettingsViewModel

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    private val viewModel by viewModels<SettingsViewModel>()

    private val navController by lazy { findNavController() }

    private val loadingDialog = LoadingDialog()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater)
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

    override fun onStart() {
        super.onStart()
        viewModel.fetchFriendshipStats()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@SettingsFragment
        viewModel = this@SettingsFragment.viewModel

        cardFriends.setOnClickListener {
            navController.navigate(NavGraphMainDirections.actionToFriendsFragment())
        }
        cardProfile.setOnClickListener {
            navController.navigate(NavGraphMainDirections.actionToOwnProfileFragment())
        }
        cardInvitation.setOnClickListener {
            navController.navigate(NavGraphMainDirections.actionToInviteAFriendFragment())
        }
        cardHistory.setOnClickListener {
            navController.navigate(SettingsFragmentDirections.actionSettingsFragmentToHistoryFragment())
        }
        cardContact.setOnClickListener {
            navController.navigate(SettingsFragmentDirections.actionSettingsFragmentToContactFragment())
        }
        cardChampions.setOnClickListener {
            navController.navigate(SettingsFragmentDirections.actionSettingsFragmentToChampionsFragment())
        }
        cardLanguage.setOnClickListener {
            navController.navigate(SettingsFragmentDirections.actionSettingsFragmentToLanguageFragment())
        }
        cardInfo.setOnClickListener {
            navController.navigate(SettingsFragmentDirections.actionSettingsFragmentToAboutUsFragment())
        }
        cardLogout.setOnClickListener {
            loadingDialog.show(requireActivity().supportFragmentManager, LOADING_TAG)
            this@SettingsFragment.viewModel.logOut()
        }
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

        eventExpiredToken.observe(viewLifecycleOwner, {
            it?.let {
                if (it) {
                    endExpireTokenProcess()
                    requireActivity().logout()
                }
            }
        })

        eventLogOut.observe(viewLifecycleOwner, {
            it?.let {
                if (it) {
                    endLogOut()
                    loadingDialog.dismiss()
                    requireActivity().logout()
                }
            }
        })

    }

}