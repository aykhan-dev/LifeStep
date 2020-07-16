package az.rabita.lifestep.ui.custom

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import az.rabita.lifestep.R
import kotlinx.android.synthetic.main.layout_custom_switch.view.*

class CustomSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defRes: Int = 0
) : ConstraintLayout(context, attrs, defStyle, defRes) {

    private var onSwitch: ((isSwitchedOn: Boolean) -> Unit)? = null

    var isSwitchOn = false
        set(value) {
            thumb.backgroundTintList =
                ColorStateList.valueOf(if (value) selectedThumbColor else unselectedThumbColor)

            thumb.layoutParams = (thumb.layoutParams as LayoutParams).apply {
                horizontalBias = if (value) 1f else 0f
            }

            backgroundTintList =
                ColorStateList.valueOf(if (value) selectedBackgroundColor else unselectedBackgroundColor)

            field = value
        }

    var unselectedThumbColor = Color.parseColor("#FFFFFF")
    var selectedThumbColor = Color.parseColor("#FFFFFF")

    var unselectedBackgroundColor = Color.parseColor("#7B8085")
    var selectedBackgroundColor = Color.parseColor("#353A85")

    init {
        View.inflate(context, R.layout.layout_custom_switch, this)

        setOnClickListener { onSwitched() }

        with(context.obtainStyledAttributes(attrs, R.styleable.CustomSwitch)) {
            //can be developed
        }
    }

    private fun onSwitched() {
        isSwitchOn = !isSwitchOn
        onSwitch?.invoke(isSwitchOn)
    }

    fun setOnSwitchListener(listener: (isSwitchedOn: Boolean) -> Unit) {
        onSwitch = listener
    }

}