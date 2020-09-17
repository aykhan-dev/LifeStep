package az.rabita.lifestep.ui.fragment.ownProfileDetails


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
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentOwnProfileDetailsBinding

import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.utils.shortenString
import az.rabita.lifestep.viewModel.fragment.profileDetails.RefactoredOwnProfileViewModel

class OwnProfileDetailsFragment : Fragment() {

    private lateinit var binding: FragmentOwnProfileDetailsBinding

    private val viewModel by viewModels<RefactoredOwnProfileViewModel>()

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOwnProfileDetailsBinding.inflate(inflater)
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
        //viewModel.fetchProfile()
        viewModel.fetchAllInOneProfileInfo()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@OwnProfileDetailsFragment
        viewModel = this@OwnProfileDetailsFragment.viewModel

        imageButtonBack.setOnClickListener { activity?.onBackPressed() }
        imageButtonEditProfile.setOnClickListener {
            navController.navigate(
                OwnProfileDetailsFragmentDirections.actionProfileDetailsFragmentToEditProfileFragment()
            )
        }
    }

    private fun observeData(): Unit = with(viewModel) {

        cachedProfileInfo.observe(viewLifecycleOwner, Observer {
            it?.let {

                binding.textViewFullName.text =
                    getString(R.string.two_lined_format, it.name, it.surname)

                val spannableFriends =
                    SpannableString("${it.friendsCount}\n${getString(R.string.friends)}")
                val spannableSteps =
                    SpannableString(
                        "${
                            requireContext().shortenString(
                                it.balance,
                                6
                            )
                        }\n${getString(R.string.total_steps)}"
                    )

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

        dailyStats.observe(viewLifecycleOwner, Observer {
            it?.let { if (isDailyStatsShown.value == true) binding.diagram.submitData(it) }
        })

        monthlyStats.observe(viewLifecycleOwner, Observer {
            it?.let { if (isDailyStatsShown.value == false) binding.diagram.submitData(it) }
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

}