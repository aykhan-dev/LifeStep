package az.rabita.lifestep.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import az.rabita.lifestep.R
import kotlinx.android.synthetic.main.layout_daily_stat_card.view.*

class DailyStatCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defRes: Int = 0
) : ConstraintLayout(context, attrs, defStyle, defRes) {

    var count = "0"
        set(value) {
            text_view_count.text = value
            field = value
        }

    init {
        View.inflate(context, R.layout.layout_daily_stat_card, this)

        clipChildren = false
        clipToPadding = false

        with(context.obtainStyledAttributes(attrs, R.styleable.DailyStatCard)) {

            val imgSrc = getResourceId(R.styleable.DailyStatCard_daily_stat_card_image, 0)

            val titleText = getString(R.styleable.DailyStatCard_daily_stat_card_title)

            image_view.setImageResource(imgSrc)

            text_view_title.text = titleText

            recycle()
        }

    }

}