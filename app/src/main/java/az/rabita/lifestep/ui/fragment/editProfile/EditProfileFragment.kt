package az.rabita.lifestep.ui.fragment.editProfile

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.databinding.FragmentEditProfileBinding
import az.rabita.lifestep.ui.activity.forgotPassword.ForgotPasswordActivity
import az.rabita.lifestep.ui.dialog.imagePicker.ImagePickerSheet
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.SingleMessageDialog
import az.rabita.lifestep.utils.*
import az.rabita.lifestep.viewModel.fragment.editProfile.EditProfileViewModel
import timber.log.Timber
import java.util.*


class EditProfileFragment : Fragment() {

    companion object {
        const val CAMERA = 100
        const val GALLERY = 200
    }

    private lateinit var binding: FragmentEditProfileBinding

    private val viewModel by viewModels<EditProfileViewModel>()

    private val navController by lazy { findNavController() }

    private val loadingDialog = LoadingDialog

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

        root.setOnClickListener { root.hideKeyboard(context) }
        constraintLayoutContent.setOnClickListener { root.hideKeyboard(context) }
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
                activity?.let { activity ->
                    SingleMessageDialog.popUp(
                        activity.supportFragmentManager,
                        ERROR_TAG,
                        errorMsg
                    )
                }
            }
        })

    }

    private fun observeStates(): Unit = with(viewModel) {

        uiState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    is UiState.Loading -> activity?.supportFragmentManager?.let { fm ->
                        loadingDialog.show(
                            fm,
                            LOADING_TAG
                        )
                    }
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
        ImagePickerSheet(
            onCameraSelect = {
                val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(takePicture, CAMERA)
            },
            onGallerySelect = {
                val takePicture = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(takePicture, GALLERY)
            }
        ).show(requireActivity().supportFragmentManager, "Image Picker")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when (requestCode) {
            CAMERA, GALLERY -> {
                if (resultCode == RESULT_OK) {
                    intent?.data?.let { uri ->
                        val bitmap = requireContext().getBitmapFromUri(uri)
                        val byteArray = bitmap.toByteArray(Bitmap.CompressFormat.JPEG, 50)
                        val file = requireContext().convertByteArrayToFile(
                            byteArray = byteArray,
                            path = "${UUID.randomUUID()}.jpeg"
                        )
                        file?.let(viewModel::updateProfileImage)
                    }
                } else Timber.e("Result failed")
            }
        }
    }

}