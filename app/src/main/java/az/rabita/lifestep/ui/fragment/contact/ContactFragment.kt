package az.rabita.lifestep.ui.fragment.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.databinding.FragmentContactBinding
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.VerticalSpaceItemDecoration
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.utils.pxFromDp
import az.rabita.lifestep.viewModel.fragment.contact.ContactViewModel

class ContactFragment : Fragment() {

    private lateinit var binding: FragmentContactBinding

    private val viewModel: ContactViewModel by viewModels()

    private val contactsAdapter = ContactRecyclerAdapter
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactBinding.inflate(inflater)

        binding.apply {
            lifecycleOwner = this@ContactFragment
            viewModel = this@ContactFragment.viewModel
        }

        with(binding) {
            imageButtonBack.setOnClickListener { navController.popBackStack() }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchContactPageContent()
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

    private fun configureRecyclerView() = with(binding.recyclerViewContacts) {
        adapter = contactsAdapter

        context?.let {
            addItemDecoration(
                VerticalSpaceItemDecoration(pxFromDp(it, 20f), pxFromDp(it, 90f))
            )
        }
    }

    private fun observeData(): Unit = with(viewModel) {

        listOfContacts.observe(viewLifecycleOwner, Observer {
            it?.let { contactsAdapter.submitList(it) }
        })

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

}