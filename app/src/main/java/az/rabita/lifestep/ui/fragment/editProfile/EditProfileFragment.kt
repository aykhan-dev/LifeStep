package az.rabita.lifestep.ui.fragment.editProfile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.databinding.FragmentEditProfileBinding
import az.rabita.lifestep.ui.activity.forgotPassword.ForgotPasswordActivity
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.MAIN_TO_FORGOT_PASSWORD_KEY
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.editProfile.EditProfileViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


class EditProfileFragment : Fragment() {

    private val PICK_IMAGE = 1
    private val PIC_CROP = 2

    private lateinit var binding: FragmentEditProfileBinding

    private val viewModel: EditProfileViewModel by viewModels()

    private val loadingDialog by lazy { LoadingDialog() }

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater)

        binding.apply {
            lifecycleOwner = this@EditProfileFragment
            viewModel = this@EditProfileFragment.viewModel
        }

        with(binding) {
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
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchPersonalInfo()
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
        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
        getIntent.type = "image/*"

        val pickIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickIntent.type = "image/*"

        val chooserIntent = Intent.createChooser(getIntent, "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

        startActivityForResult(chooserIntent, PICK_IMAGE)
    }

    private fun sendImageToCrop(uri: Uri?) {
        try {
            val cropIntent = Intent("com.android.camera.action.CROP")
            // indicate image type and Uri
            cropIntent.setDataAndType(uri, "image/*")
            // set crop properties here
            cropIntent.putExtra("crop", true)
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1)
            cropIntent.putExtra("aspectY", 1)
            // indicate output X and Y
            cropIntent.putExtra("outputX", 128)
            cropIntent.putExtra("outputY", 128)
            // retrieve data on return
            cropIntent.putExtra("return-data", true)
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP)
        } // respond to users whose devices do not support the crop action
        catch (anfe: ActivityNotFoundException) {
            // display an error message
            val errorMessage =
                "Whoops - your device doesn't support the crop action!"
            val toast: Toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PICK_IMAGE -> sendImageToCrop(data?.data)
            PIC_CROP -> {
                val extras = data?.extras
                val selectedBitmap = extras?.getParcelable<Bitmap>("data")
                selectedBitmap?.let {
                    context?.let {
                        val file = File(it.cacheDir, UUID.randomUUID().toString() + ".png")
                        val bos = ByteArrayOutputStream()
                        selectedBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
                        val bitmapData = bos.toByteArray()
                        val fos = FileOutputStream(file)
                        fos.write(bitmapData)
                        fos.flush()
                        fos.close()
                        viewModel.updateProfileImage(file)
                    }
                }
            }
        }
    }

}
