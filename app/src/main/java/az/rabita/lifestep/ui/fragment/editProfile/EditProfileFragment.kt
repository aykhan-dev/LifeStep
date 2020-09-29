package az.rabita.lifestep.ui.fragment.editProfile

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.rabita.lifestep.databinding.FragmentEditProfileBinding
import az.rabita.lifestep.ui.activity.forgotPassword.ForgotPasswordActivity
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.SingleMessageDialog
import az.rabita.lifestep.utils.*
import az.rabita.lifestep.viewModel.fragment.editProfile.EditProfileViewModel
import com.vansuita.pickimage.bundle.PickSetup
import com.vansuita.pickimage.dialog.PickImageDialog
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

        profileInfo.observe(viewLifecycleOwner, {
            it?.let {
                viewModel.nameInput.postValue(it.name)
                viewModel.surnameInput.postValue(it.surname)
                viewModel.phoneInput.postValue(it.phone)
            }
        })

        errorMessage.observe(viewLifecycleOwner, {
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

        uiState.observe(viewLifecycleOwner, {
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

        eventCloseEditProfilePage.observe(viewLifecycleOwner, {
            it?.let {
                if (it) activity?.onBackPressed()
            }
        })

        eventExpiredToken.observe(viewLifecycleOwner, {
            it?.let {
                if (it) {
                    activity?.logout()
                    endExpireTokenProcess()
                }
            }
        })

    }

    private fun pickImage() {

        PickImageDialog.build(PickSetup())
            .setOnPickResult {
                it?.let {
                    Timber.e("✅ file uri has been received ${Calendar.getInstance().time}")
                    val bitmap = it.bitmap
                    val byteArray = bitmap.toByteArray(Bitmap.CompressFormat.JPEG, 50)
                    val file = requireContext().convertByteArrayToFile(
                        byteArray = byteArray,
                        path = "${UUID.randomUUID()}.jpeg"
                    )
                    Timber.e("✅ file has been created ${Calendar.getInstance().time}")
                    file?.let(viewModel::updateProfileImage)
                }
            }
            .show(parentFragmentManager)

    }

}