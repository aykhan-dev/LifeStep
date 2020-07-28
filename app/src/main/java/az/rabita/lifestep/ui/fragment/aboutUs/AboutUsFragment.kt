package az.rabita.lifestep.ui.fragment.aboutUs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.databinding.FragmentAboutUsBinding
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.aboutUs.AboutUsViewModel

class AboutUsFragment : Fragment() {

    private lateinit var binding: FragmentAboutUsBinding

    private val viewModel: AboutUsViewModel by viewModels()

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAboutUsBinding.inflate(inflater)

        binding.apply {
            lifecycleOwner = this@AboutUsFragment
            viewModel = this@AboutUsFragment.viewModel
        }

        with(binding) {
            imageButtonBack.setOnClickListener { navController.popBackStack() }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchAboutUsPageContent()
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
