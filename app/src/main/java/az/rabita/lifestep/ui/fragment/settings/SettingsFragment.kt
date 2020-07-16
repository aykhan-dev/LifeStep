package az.rabita.lifestep.ui.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.databinding.FragmentSettingsBinding
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.TOKEN_KEY
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.settings.SettingsViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    private val viewModel: SettingsViewModel by viewModels()

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater)

        binding.apply {
            lifecycleOwner = this@SettingsFragment
            viewModel = this@SettingsFragment.viewModel
        }

        with(binding) {
            cardInfo.setOnClickListener { navigateTo(SettingsPageDirections.INFO) }
            cardLogout.setOnClickListener { navigateTo(SettingsPageDirections.LOGOUT) }
            cardContact.setOnClickListener { navigateTo(SettingsPageDirections.CONTACT) }
            cardLanguage.setOnClickListener { navigateTo(SettingsPageDirections.LANGUAGE) }
            cardFriends.setOnClickListener { navigateTo(SettingsPageDirections.FRIENDS) }
            cardInvitation.setOnClickListener { navigateTo(SettingsPageDirections.INVITATION) }
            cardChampions.setOnClickListener { navigateTo(SettingsPageDirections.CHAMPIONS) }
            cardHistory.setOnClickListener { navigateTo(SettingsPageDirections.HISTORY) }
            cardProfile.setOnClickListener { navigateTo(SettingsPageDirections.PROFILE) }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchFriendshipStats()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
    }

    private fun navigateTo(direction: SettingsPageDirections) {
        val destination = when (direction) {
            SettingsPageDirections.FRIENDS -> SettingsFragmentDirections.actionSettingsFragmentToFriendsFragment()
            SettingsPageDirections.PROFILE -> SettingsFragmentDirections.actionSettingsFragmentToNavGraphProfile()
            SettingsPageDirections.INVITATION -> SettingsFragmentDirections.actionSettingsFragmentToInviteFriendFragment()
            SettingsPageDirections.HISTORY -> SettingsFragmentDirections.actionSettingsFragmentToHistoryFragment()
            SettingsPageDirections.CONTACT -> SettingsFragmentDirections.actionSettingsFragmentToContactFragment()
            SettingsPageDirections.CHAMPIONS -> SettingsFragmentDirections.actionSettingsFragmentToRankingFragment(
                postId = null
            )
            SettingsPageDirections.LANGUAGE -> SettingsFragmentDirections.actionSettingsFragmentToLanguageFragment()
            SettingsPageDirections.INFO -> SettingsFragmentDirections.actionSettingsFragmentToAboutUsFragment()
            SettingsPageDirections.LOGOUT -> {
                context?.let {
                    signOutGoogle()
                    PreferenceManager.getInstance(it).setStringElement(TOKEN_KEY, "")
                    requireActivity().logout()
                }
                return
            }
        }
        navController.navigate(destination)
    }

    private fun observeData(): Unit = with(viewModel) {

        errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let {
                activity?.let { activity ->
                    MessageDialog(MessageType.ERROR, it).show(
                        activity.supportFragmentManager,
                        ERROR_TAG
                    )
                }
            }
        })

    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventExpiredToken.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    activity?.logout()
                    endExpireTokenProcess()
                }
            }
        })

    }

    private fun signOutGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        googleSignInClient.signOut()
    }

}
