package az.rabita.lifestep.ui.fragment.editProfile

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
import az.rabita.lifestep.databinding.FragmentEditProfileBinding
import az.rabita.lifestep.ui.activity.forgotPassword.ForgotPasswordActivity
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.utils.*
import az.rabita.lifestep.viewModel.fragment.editProfile.EditProfileViewModel
import com.vansuita.pickimage.bundle.PickSetup
import com.vansuita.pickimage.dialog.PickImageDialog

class EditProfileFragment : Fragment() {

    companion object {
        const val CAMERA = 100
        const val GALLERY = 200
    }

    private lateinit var binding: FragmentEditProfileBinding

    private val viewModel by viewModels<EditProfileViewModel>()

    private val navController by lazy { findNavController() }

    private val loadingDialog = LoadingDialog()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeStates()
        observeEvents()
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchPersonalInfo()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@EditProfileFragment
        viewModel = this@EditProfileFragment.viewModel

        imageButtonBack.setOnClickListener { navController.popBackStack() }
        imageViewEditImage.setOnClickListener { pickImage() }
        buttonChangePassword.setOnClickListener {
            activity?.let {
                val intent = Intent(it, ForgotPasswordActivity::class.java)
                intent.putExtra(MAIN_TO_FORGOT_PASSWORD_KEY, true)
                startActivity(intent)
            }
        }

        editTextEmail.isEnabled = false
        editTextInvitationCode.isEnabled = false

        root.setOnClickListener { root.hideKeyboard() }
        constraintLayoutContent.setOnClickListener { root.hideKeyboard() }
    }

    private fun observeData(): Unit = with(viewModel) {

        profileInfo.observe(viewLifecycleOwner, Observer {
            it?.let {
                viewModel.nameInput.postValue(it.name)
                viewModel.surnameInput.postValue(it.surname)
                viewModel.phoneInput.postValue(it.phone)
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

    private fun observeStates(): Unit = with(viewModel) {

        uiState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    is UiState.Loading -> loadingDialog.show(
                        requireActivity().supportFragmentManager,
                        ERROR_TAG
                    )
                    is UiState.LoadingFinished -> {
                        loadingDialog.dismiss()
                        uiState.value = null
                    }
                }
            }
        })

    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventCloseEditProfilePage.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) activity?.onBackPressed()
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

    private fun pickImage() {

        PickImageDialog.build(
            PickSetup().apply {
                title = getString(R.string.choose_image)
                cameraButtonText = getString(R.string.camera)
                galleryButtonText = getString(R.string.gallery)
            }
        ).setOnPickResult {
            it?.let {
                navController.navigate(
                    EditProfileFragmentDirections.actionEditProfileFragmentToCroppingActivity(
                        it.uri.toString()
                    )
                )
            }
        }.show(parentFragmentManager)

    }

}