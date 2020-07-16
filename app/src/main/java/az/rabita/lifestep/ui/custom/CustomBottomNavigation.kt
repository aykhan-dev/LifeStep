package az.rabita.lifestep.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import az.rabita.lifestep.R
import az.rabita.lifestep.utils.pxFromDp
import com.google.android.material.bottomnavigation.BottomNavigationView

class CustomBottomNavigation @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BottomNavigationView(context, attrs, defStyle) {

    private var mPath: Path = Path()
    private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    //the CURVE_CIRCLE_RADIUS represent the radius of the fab button
    private val CURVE_CIRCLE_RADIUS = pxFromDp(getContext(), 70f) / 2

    // the coordinates of the first curve
    private val mFirstCurveStartPoint = Point()
    private val mFirstCurveEndPoint = Point()
    private val mFirstCurveControlPoint1 = Point()
    private val mFirstCurveControlPoint2 = Point()

    //the coordinates of the second curve
    private var mSecondCurveStartPoint = Point()
    private val mSecondCurveEndPoint = Point()
    private val mSecondCurveControlPoint1 = Point()
    private val mSecondCurveControlPoint2 = Point()
    private var mNavigationBarWidth = 0
    private var mNavigationBarHeight = 0

    init {
        mPaint.style = Paint.Style.FILL_AND_STROKE
        mPaint.color = ContextCompat.getColor(getContext(), R.color.bottom_navigation_view_color)
        setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // get width and height of navigation bar
        // Navigation bar bounds (width & height)
        mNavigationBarWidth = width
        mNavigationBarHeight = height
        // the coordinates (x,y) of the start point before curve
        mFirstCurveStartPoint[(mNavigationBarWidth / 2 - CURVE_CIRCLE_RADIUS * 1.99).toInt()] =
            0
        // the coordinates (x,y) of the end point after curve
        mFirstCurveEndPoint[mNavigationBarWidth / 2] = (CURVE_CIRCLE_RADIUS * 1.2).toInt()
        // same thing for the second curve
        mSecondCurveStartPoint = mFirstCurveEndPoint
        mSecondCurveEndPoint[(mNavigationBarWidth / 2 + CURVE_CIRCLE_RADIUS * 1.99).toInt()] = 0

        // the coordinates (x,y)  of the 1st control point on a cubic curve
        mFirstCurveControlPoint1[(mFirstCurveStartPoint.x + mFirstCurveEndPoint.x) / 2] =
            mFirstCurveStartPoint.y
        // the coordinates (x,y)  of the 2nd control point on a cubic curve
        mFirstCurveControlPoint2[((mFirstCurveStartPoint.x + mFirstCurveEndPoint.x) / 2.1).toInt()] =
            mFirstCurveEndPoint.y
        mSecondCurveControlPoint1[((mSecondCurveStartPoint.x + mSecondCurveEndPoint.x) / 1.9).toInt()] =
            mSecondCurveStartPoint.y
        mSecondCurveControlPoint2[(mSecondCurveStartPoint.x + mSecondCurveEndPoint.x) / 2] =
            mSecondCurveEndPoint.y
        mPath.reset()
        mPath.moveTo(0f, (mNavigationBarHeight / 2).toFloat())
        mPath.cubicTo(
            0f,
            (CURVE_CIRCLE_RADIUS / 2).toFloat(),
            (CURVE_CIRCLE_RADIUS / 2).toFloat(),
            0f,
            CURVE_CIRCLE_RADIUS.toFloat(),
            0f
        )
        mPath.lineTo(mFirstCurveStartPoint.x.toFloat(), mFirstCurveStartPoint.y.toFloat())
        mPath.cubicTo(
            mFirstCurveControlPoint1.x.toFloat(), mFirstCurveControlPoint1.y.toFloat(),
            mFirstCurveControlPoint2.x.toFloat(), mFirstCurveControlPoint2.y.toFloat(),
            mFirstCurveEndPoint.x.toFloat(), mFirstCurveEndPoint.y.toFloat()
        )
        mPath.cubicTo(
            mSecondCurveControlPoint1.x.toFloat(), mSecondCurveControlPoint1.y.toFloat(),
            mSecondCurveControlPoint2.x.toFloat(), mSecondCurveControlPoint2.y.toFloat(),
            mSecondCurveEndPoint.x.toFloat(), mSecondCurveEndPoint.y.toFloat()
        )
        mPath.lineTo(mNavigationBarWidth - CURVE_CIRCLE_RADIUS.toFloat(), 0f)
        mPath.cubicTo(
            mNavigationBarWidth - (CURVE_CIRCLE_RADIUS / 2).toFloat(),
            0f,
            mNavigationBarWidth.toFloat(),
            (CURVE_CIRCLE_RADIUS / 2).toFloat(),
            mNavigationBarWidth.toFloat(),
            (mNavigationBarHeight / 2).toFloat()
        )
        mPath.lineTo(mNavigationBarWidth.toFloat(), mNavigationBarHeight.toFloat())
        mPath.lineTo(0f, mNavigationBarHeight.toFloat())
        mPath.close()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(mPath, mPaint)
    }

}