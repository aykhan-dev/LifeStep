package az.rabita.lifestep.ui.fragment.userProfile

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentUserProfileBinding
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.profileDetails.UserProfileViewModel

class UserProfileFragment : Fragment() {

    private lateinit var binding: FragmentUserProfileBinding

    private val viewModel by viewModels<UserProfileViewModel>()
    private val args by navArgs<UserProfileFragmentArgs>()

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchUserProfileDetails(args.userId)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@UserProfileFragment
        viewModel = this@UserProfileFragment.viewModel

        imageButtonBack.setOnClickListener { activity?.onBackPressed() }
        buttonSendSteps.setOnClickListener { this@UserProfileFragment.viewModel.fetchPersonalInfo() }
    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventShowSendStepsDialog.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    profileInfo.value?.let { info ->
                        navController.navigate(
                            UserProfileFragmentDirections.actionUserProfileFragmentToSendStepDialogFragment(
                                info.id
                            )
                        )
                    }
                }
            }
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

    private fun observeData(): Unit = with(viewModel) {

        //DON'T REMOVE THIS LINE
        personalInfo.observe(viewLifecycleOwner, Observer { })

        friendshipStatus.observe(viewLifecycleOwner, Observer {
            it?.let {
                with(binding) {
                    buttonSendFriendRequest.text = profileInfo.value?.friendShipStatusMessage
                    when (it) {
                        FriendshipStatus.PENDING -> buttonSendFriendRequest.isEnabled = false
                        FriendshipStatus.NOT_FRIEND -> buttonSendFriendRequest.setOnClickListener {
                            this@UserProfileFragment.viewModel.sendFriendRequest()
                        }
                    }
                }
            }
        })

        profileInfo.observe(viewLifecycleOwner, Observer {
            it?.let {

                binding.textViewFullName.text =
                    getString(R.string.two_lined_format, it.surname, it.name)

                if (it.id == personalInfo.value?.id ?: "") {
                    binding.buttonSendFriendRequest.isVisible = false
                    binding.buttonSendSteps.isVisible = false
                }

                val spannableFriends =
                    SpannableString("${it.friendsCount}\n${getString(R.string.friends)}")
                val spannableSteps =
                    SpannableString("${it.balance}\n${getString(R.string.total_steps)}")

                spannableFriends.setSpan(
                    RelativeSizeSpan(2f),
                    0, spannableFriends.indexOf("\n"),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                spannableSteps.setSpan(
                    RelativeSizeSpan(2f),
                    0, spannableSteps.indexOf("\n"),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                spannableFriends.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, spannableFriends.indexOf("\n"),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                spannableSteps.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, spannableSteps.indexOf("\n"),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                binding.textViewFriends.setText(spannableFriends, TextView.BufferType.SPANNABLE)
                binding.textViewTotalSteps.setText(spannableSteps, TextView.BufferType.SPANNABLE)
            }
        })

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

}