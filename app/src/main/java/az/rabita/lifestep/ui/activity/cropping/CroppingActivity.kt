package az.rabita.lifestep.ui.activity.cropping

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.navigation.navArgs
import az.rabita.lifestep.databinding.ActivityCroppingBinding
import az.rabita.lifestep.ui.activity.BaseActivity
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.UiState
import az.rabita.lifestep.utils.convertByteArrayToFile
import az.rabita.lifestep.utils.toByteArray
import az.rabita.lifestep.viewModel.fragment.editProfile.EditProfileViewModel
import java.util.*

class CroppingActivity : BaseActivity() {

    private val binding by lazy { ActivityCroppingBinding.inflate(layoutInflater) }
    private val args by navArgs<CroppingActivityArgs>()
    private val viewModel by viewModels<EditProfileViewModel>()

    private val loadingDialog = LoadingDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener { onBackPressed() }
        binding.buttonCrop.setOnClickListener {
            val bitmap = binding.cropper.croppedImage
            val byteArray = bitmap.toByteArray(Bitmap.CompressFormat.JPEG, 50)
            val file = applicationContext.convertByteArrayToFile(
                byteArray = byteArray,
                path = "${UUID.randomUUID()}.jpeg"
            )
            file?.let(viewModel::updateProfileImage)
        }

        with(binding.cropper) {
            setAspectRatio(1, 1)
            setImageUriAsync(args.imageUri.toUri())
        }

        viewModel.uiState.observe(this, Observer {
            it?.let {
                when (it) {
                    is UiState.Loading -> loadingDialog.show(
                        supportFragmentManager,
                        ERROR_TAG
                    )
                    is UiState.LoadingFinished -> {
                        loadingDialog.dismiss()
                        viewModel.uiState.value = null
                        finish()
                    }
                }
            }
        })

    }

}