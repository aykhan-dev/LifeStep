package az.rabita.lifestep.ui.dialog.imagePicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import az.rabita.lifestep.databinding.LayoutImagePickerSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ImagePickerSheet(
    private val onCameraSelect: () -> Unit,
    private val onGallerySelect: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: LayoutImagePickerSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = LayoutImagePickerSheetBinding.inflate(inflater).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
    }

    private fun bindUI(): Unit = with(binding) {
        textViewCamera.setOnClickListener {
            onCameraSelect()
            dismiss()
        }
        textViewGallery.setOnClickListener {
            onGallerySelect()
            dismiss()
        }
    }

}