package az.rabita.lifestep.ui.fragment.otherUserProfile

import android.content.Intent
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentUserProfileBinding
import az.rabita.lifestep.pojo.dataHolder.UserProfileInfoHolder
import az.rabita.lifestep.ui.activity.imageReview.ImageReviewActivity
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.sendStep.SendStepDialog
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.LOADING_TAG
import az.rabita.lifestep.utils.UiState
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.profileDetails.OtherUserProfileViewModel

class OtherUserProfileFragment : Fragment() {

    private lateinit var binding: FragmentUserProfileBinding

    private val viewModel by viewModels<OtherUserProfileViewModel>()
    private val args by navArgs<OtherUserProfileFragmentArgs>()

    private val navController by lazy { findNavController() }

    private val loadingDialog = LoadingDialog()

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
        with(viewModel) {
            updateOwnProfileData()
            fetchAllInOneProfileInfo(args.userId)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
        observeStates()
        retrieveStepSendingResult()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@OtherUserProfileFragment
        viewModel = this@OtherUserProfileFragment.viewModel

        imageButtonBack.setOnClickListener { activity?.onBackPressed() }
        buttonSendSteps.setOnClickListener {
            with(this@OtherUserProfileFragment.viewModel) {
                profileInfo.value?.let { info ->
                    navController.navigate(
                        OtherUserProfileFragmentDirections.actionOtherUserProfileFragmentToSendStepDialog(
                            UserProfileInfoHolder(
                                info.id,
                                cachedOwnProfileInfo.value?.balance ?: 0
                            )
                        )
                    )
                }
            }
        }
        imageViewProfile.setOnClickListener {
            this@OtherUserProfileFragment.viewModel.profileInfo.value?.let {
                val intent = Intent(requireActivity(), ImageReviewActivity::class.java)
                intent.putExtra("profileImageUrl", it.originalUrl)
                startActivity(intent)
            }
        }
    }

    private fun observeStates(): Unit = with(viewModel) {

        uiState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    is UiState.Loading -> loadingDialog.show(
                        requireActivity().supportFragmentManager,
                        LOADING_TAG
                    )
                    is UiState.LoadingFinished -> loadingDialog.dismiss()
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

    private fun observeData(): Unit = with(viewModel) {

        //DON'T REMOVE THIS LINE ELSE IT WILL BE NULL
        cachedOwnProfileInfo.observe(viewLifecycleOwner, Observer { })

        friendshipStatus.observe(viewLifecycleOwner, Observer {
            it?.let {
                with(binding) {
                    buttonSendFriendRequest.text = profileInfo.value?.friendShipStatusMessage
                    when (it) {
                        FriendshipStatus.PENDING -> buttonSendFriendRequest.isEnabled = false
                        FriendshipStatus.NOT_FRIEND -> buttonSendFriendRequest.setOnClickListener {
                            this@OtherUserProfileFragment.viewModel.sendFriendRequest()
                        }
                        else -> kotlin.run { }
                    }
                }
            }
        })

        dailyStats.observe(viewLifecycleOwner, Observer {
            it?.let { if (isDailyStatsShown.value == true) binding.diagram.submitData(it) }
        })

        monthlyStats.observe(viewLifecycleOwner, Observer {
            it?.let { if (isDailyStatsShown.value == false) binding.diagram.submitData(it) }
        })

        profileInfo.observe(viewLifecycleOwner, Observer {
            it?.let {

                binding.textViewFullName.text =
                    getString(R.string.two_lined_format, it.surname, it.name)

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
            it?.let { errorMsg ->
                MessageDialog.getInstance(errorMsg).show(
                    requireActivity().supportFragmentManager,
                    ERROR_TAG
                )
            }
        })

    }

    private fun retrieveStepSendingResult() {

        val stateHandle = navController.currentBackStackEntry!!.savedStateHandle

        stateHandle.getLiveData<Map<String, Any>>(SendStepDialog.RESULT_KEY)
            .observe(viewLifecycleOwner, Observer {
                it?.let { data ->
                    val amount = data[SendStepDialog.AMOUNT_KEY] as Long
                    viewModel.sendStep(amount)
                }
            })

    }

}