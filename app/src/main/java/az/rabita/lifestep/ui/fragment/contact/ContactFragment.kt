package az.rabita.lifestep.ui.fragment.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.NavGraphMainDirections
import az.rabita.lifestep.databinding.FragmentContactBinding
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.utils.*
import az.rabita.lifestep.viewModel.fragment.contact.ContactViewModel

class ContactFragment : Fragment() {

    private lateinit var binding: FragmentContactBinding

    private val viewModel by viewModels<ContactViewModel>()

    private val contactsAdapter = ContactRecyclerAdapter {
        if (it.contentKey == "mobilephone" || it.contentKey == "phone") callNumber(it.content)
    }
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactBinding.inflate(inflater)
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
        viewModel.fetchContactPageContent()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@ContactFragment
        viewModel = this@ContactFragment.viewModel

        imageButtonBack.setOnClickListener { navController.popBackStack() }
    }

    private fun configureRecyclerView(): Unit = with(binding.recyclerViewContacts) {
        adapter = contactsAdapter

        context?.let {
            addItemDecoration(VerticalSpaceItemDecoration(pxFromDp(it, 20f), pxFromDp(it, 0f)))
        }
    }

    private fun observeData(): Unit = with(viewModel) {

        listOfContacts.observe(viewLifecycleOwner, {
            it?.let { contactsAdapter.submitList(it) }
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

}