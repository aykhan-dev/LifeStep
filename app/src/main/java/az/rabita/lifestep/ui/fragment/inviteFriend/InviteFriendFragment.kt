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
import az.rabita.lifestep.ui.dialog.message.SingleMessageDialog
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.INVITE_TEXT_KEY
import az.rabita.lifestep.utils.NO_MESSAGE
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.inviteFriend.InviteFriendViewModel

class InviteFriendFragment : Fragment() {

    private lateinit var binding: FragmentInviteFriendBinding

    private val viewModel by viewModels<InviteFriendViewModel>()

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInviteFriendBinding.inflate(inflater)
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
        viewModel.fetchPersonalInfo()
        viewModel.fetchInviteFriendsContent()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@InviteFriendFragment
        viewModel = this@InviteFriendFragment.viewModel

        imageButtonBack.setOnClickListener { navController.popBackStack() }
        editTextInvitationCode.isEnabled = false
    }

    private fun observeData(): Unit = with(viewModel) {

        errorMessage.observe(viewLifecycleOwner, Observer {
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

        //It is needed, else inviteFriendContent's value will be null
        inviteFriendContentMessage.observe(viewLifecycleOwner, Observer { })

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

        val message = viewModel.inviteFriendContentMessage.value?.content ?: NO_MESSAGE

        shareIntent.putExtra(Intent.EXTRA_TEXT, message)
        startActivity(Intent.createChooser(shareIntent, getString(R.string.sharing_method)))
    }

}
