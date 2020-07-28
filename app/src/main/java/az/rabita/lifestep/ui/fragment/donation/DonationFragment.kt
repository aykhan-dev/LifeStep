package az.rabita.lifestep.ui.fragment.donation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.databinding.FragmentDonationBinding
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.VerticalSpaceItemDecoration
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.utils.pxFromDp
import az.rabita.lifestep.viewModel.fragment.donation.DonationViewModel

class DonationFragment : Fragment() {

    private lateinit var binding: FragmentDonationBinding

    private val viewModel: DonationViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView

    private val donationAdapter: DonationRecyclerAdapter by lazy {
        DonationRecyclerAdapter { id -> navigateToDetailedFragment(id) }
    }

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDonationBinding.inflate(inflater)

        binding.apply {
            lifecycleOwner = this@DonationFragment
        }

        with(binding) {
            imageButtonBack.setOnClickListener { navController.popBackStack() }

            this@DonationFragment.recyclerView = recyclerViewDonation
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchListOfDonations(1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureRecyclerView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
    }

    private fun configureRecyclerView() = with(binding.recyclerViewDonation) {
        adapter = donationAdapter
    }

    private fun observeData(): Unit = with(viewModel) {

        listOfAssocations.observe(viewLifecycleOwner, Observer {
            it?.let { donationAdapter.submitList(it) }
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

    private fun navigateToDetailedFragment(id: String) = with(navController) {
        navigate(DonationFragmentDirections.actionDonationFragmentToDetailedInfoFragment(id))
    }

}
