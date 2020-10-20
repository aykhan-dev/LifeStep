package az.rabita.lifestep.ui.activity.imageReview

import android.os.BaseBundle
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.ActivityImageReviewBinding
import az.rabita.lifestep.ui.activity.BaseActivity

class ImageReviewActivity : BaseActivity() {

    private lateinit var binding: ActivityImageReviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_review)
        bindUI()
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@ImageReviewActivity
        imageUrl = intent.getStringExtra("profileImageUrl")

        buttonBack.setOnClickListener {
            onBackPressed()
        }
    }

}