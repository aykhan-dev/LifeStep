package az.rabita.lifestep.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import az.rabita.lifestep.R
import az.rabita.lifestep.utils.pxFromDp
import kotlin.math.min

class OptimalCircleSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defRes: Int = 0
) : View(context, attrs, defStyle, defRes) {

    companion object {
        const val MIN = 0f
        const val MAX = 100f
        const val ANGLE_OFFSET = -90
    }

    private val arcPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }
    private val progressPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private var progressColor = 0
        set(value) {
            progressPaint.color = value
            field = value
        }
    private var arcColor = 0
        set(value) {
            arcPaint.color = value
            field = value
        }

    private var thumbSize = 0
    private val thumbDrawable: Drawable?

    private var arcLineWidth = 0
        set(value) {
            arcPaint.strokeWidth = value.toFloat()
            field = value
        }
    private var progressLineWidth = 0
        set(value) {
            progressPaint.strokeWidth = value.toFloat()
            field = value
        }

    private var progress = 0f

    private var progressSweep = 0f // is equal to (progress / valuePerDegree())
    private var progressAngle = 0f // is equal to (Math.PI / 2 - progressSweep * Math.PI / 180)

    private var centerX = 0
    private var centerY = 0
    private var radius = 0

    private var mainRect = RectF()

    init {
        progressColor = ContextCompat.getColor(context, R.color.bottom_navigation_view_color)
        arcColor = ContextCompat.getColor(context, R.color.light_gray_text_color)

        thumbSize = pxFromDp(context, 8f)
        thumbDrawable = ContextCompat.getDrawable(context, R.drawable.bg_white_round)

        arcLineWidth = pxFromDp(context, 8f)
        progressLineWidth = pxFromDp(context, 14f)

        calculateArcParams()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val min = min(width, height)
        setMeasuredDimension(min, min)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val min = min(w, h)

        val topPoint = (w - min) / 2
        val bottomPoint = topPoint + min
        val leftPoint = (h - min) / 2
        val rightPoint = leftPoint + min

        centerX = rightPoint / 2 + (w - rightPoint) / 2
        centerY = bottomPoint / 2 + (h - bottomPoint) / 2

        radius = min / 2 // min == progress diameter

        val top = h / 2 - radius
        val left = w / 2 - radius

        mainRect.set(
            left.toFloat(),
            top.toFloat(),
            (left + min).toFloat(),
            (top + min).toFloat()
        )

        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), radius.toFloat(), arcPaint)
            canvas.drawArc(mainRect, 0f, progressSweep, false, progressPaint)
        }
    }

    fun updateProgress(fraction: Float) {
        progress = fraction * 100
        progressRangeCheck()
        calculateArcParams()
        invalidate()
    }

    private fun progressRangeCheck() {
        progress = if (progress > MAX) MAX else progress
        progress = if (progress < MIN) MIN else progress
    }

    private fun calculateArcParams() {
        progressSweep = progress / valuePerDegree()
        progressAngle = (Math.PI / 2 - (progressSweep * Math.PI) / 180).toFloat()
    }

    private fun valuePerDegree(): Float = MAX / 360.0f

    fun getProgress() = progress

}