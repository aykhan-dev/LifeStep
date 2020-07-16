package az.rabita.lifestep.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import az.rabita.lifestep.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.layout_wallet_card.view.*

class WalletCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : MaterialCardView(context, attrs, defStyle) {

    private val image: ImageView

    val count: MaterialTextView
    private val title: MaterialTextView

    init {
        inflate(context, R.layout.layout_wallet_card, this)

        image = image_view

        count = text_view_count
        title = text_view_title

        with(context.obtainStyledAttributes(attrs, R.styleable.WalletCard)) {

            val imgSrc = getResourceId(R.styleable.WalletCard_image, 0)

            val titleText = getString(R.styleable.WalletCard_title)

            image.setImageResource(imgSrc)

            title.text = titleText ?: ""

            recycle()
        }

    }

}