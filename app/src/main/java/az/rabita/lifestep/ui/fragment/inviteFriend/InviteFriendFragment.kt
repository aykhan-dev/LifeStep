package az.rabita.lifestep.ui.fragment.inviteFriend

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentInviteFriendBinding
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.INVITE_TEXT_KEY
import az.rabita.lifestep.utils.NO_MESSAGE
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.inviteFriend.InviteFriendViewModel

class InviteFriendFragment : Fragment() {

    private lateinit var binding: FragmentInviteFriendBinding

    private val viewModel: InviteFriendViewModel by viewModels()

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInviteFriendBinding.inflate(inflater)

        binding.apply {
            lifecycleOwner = this@InviteFriendFragment
            viewModel = this@InviteFriendFragment.viewModel
        }

        with(binding) {
            imageButtonBack.setOnClickListener { navController.popBackStack() }
            editTextInvitationCode.isEnabled = false
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchPersonalInfo()
        viewModel.fetchInviteFriendsContent()
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
                    MessageDialog(MessageType.ERROR, it).show(
                        activity.supportFragmentManager,
                        ERROR_TAG
                    )
                }
            }
        })

    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventSendSharingMessage.observe(viewLifecycleOwner, Observer {
            it?.let { if (it) sendMessage() }
        })

        eventExpiredToken.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    activity?.logout()
                    endExpireTokenProcess()
                }
            }
        })

    }

    private fun sendMessage() {
        val shareIntent = Intent(Intent.ACTION_SEND)

        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, INVITE_TEXT_KEY)

        val message = viewModel.inviteFriendContentsMessage.value ?: NO_MESSAGE

        shareIntent.putExtra(Intent.EXTRA_TEXT, message)
        startActivity(Intent.createChooser(shareIntent, getString(R.string.sharing_method)))
    }

}
