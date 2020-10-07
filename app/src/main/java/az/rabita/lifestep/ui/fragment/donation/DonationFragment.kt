package az.rabita.lifestep.ui.fragment.donation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.databinding.FragmentDonationBinding
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.donation.DonationViewModel
import jp.wasabeef.recyclerview.animators.ScaleInAnimator

class DonationFragment : Fragment() {

    private lateinit var binding: FragmentDonationBinding

    private val viewModel by viewModels<DonationViewModel>()

    private val donationAdapter: DonationRecyclerAdapter = DonationRecyclerAdapter { id ->
        navigateToDetailedFragment(id)
    }

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDonationBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
        configureRecyclerView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchListOfAssocations(1)
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@DonationFragment

        imageButtonBack.setOnClickListener { navController.popBackStack() }
    }

    private fun configureRecyclerView(): Unit = with(binding.recyclerViewDonation) {
        adapter = donationAdapter
        itemAnimator = ScaleInAnimator().apply { addDuration = 100 }
    }

    private fun observeData(): Unit = with(viewModel) {

        listOfAssocations.observe(viewLifecycleOwner, {
            it?.let { donationAdapter.submitList(it) }
        })

        errorMessage.observe(viewLifecycleOwner, {
            it?.let { errorMsg ->
                MessageDialog.getInstance(errorMsg).show(
                    requireActivity().supportFragmentManager,
                    ERROR_TAG
                )
            }
        })

    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventExpiredToken.observe(viewLifecycleOwner, {
            it?.let {
                if (it) {
                    activity?.logout()
                    endExpireTokenProcess()
                }
            }
        })

    }

    private fun navigateToDetailedFragment(id: String): Unit = with(navController) {
        navigate(DonationFragmentDirections.actionDonationFragmentToDetailedInfoFragment(id))
    }

}
