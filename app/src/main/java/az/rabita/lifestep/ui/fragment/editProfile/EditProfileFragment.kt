package az.rabita.lifestep.ui.fragment.editProfile

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
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
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.SingleMessageDialog
import az.rabita.lifestep.utils.*
import az.rabita.lifestep.viewModel.fragment.editProfile.EditProfileViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


class EditProfileFragment : Fragment() {

    private val pickImage = 1
    private val picCrop = 2

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
        imageViewEditImage.setOnClickListener { onEditImageClick() }
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

    private fun onEditImageClick() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
            .start(requireContext(), this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source: ImageDecoder.Source = ImageDecoder
                        .createSource(requireContext().contentResolver, resultUri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(
                        requireContext().contentResolver,
                        resultUri
                    )
                }
                bitmap?.let {
                    context?.let {
                        val file = File(it.cacheDir, UUID.randomUUID().toString() + ".jpeg")
                        val bos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                        val bitmapData = bos.toByteArray()
                        val fos = FileOutputStream(file)
                        fos.write(bitmapData)
                        fos.flush()
                        fos.close()
                        viewModel.updateProfileImage(file)
                    }
                } ?: context?.toast("Bitmap null")
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                context?.toast("Error while cropping")
            }
        }
    }

}