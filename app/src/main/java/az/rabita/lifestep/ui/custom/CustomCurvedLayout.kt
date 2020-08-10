package az.rabita.lifestep.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import az.rabita.lifestep.R
import az.rabita.lifestep.utils.pxFromDp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.cancel
import timber.log.Timber

class CustomCurvedLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defRes: Int = 0
) : ConstraintLayout(context, attrs, defStyle, defRes) {

    private val viewScope = CoroutineScope(Main)

    private val ids = listOf(
        R.id.nav_graph_categories,
        R.id.walletFragment,
        R.id.homeFragment,
        R.id.nav_graph_ads,
        R.id.nav_graph_settings
    )

    private val destinations = listOf(
        R.id.eventsFragment,
        R.id.walletFragment,
        R.id.homeFragment,
        R.id.adsFragment,
        R.id.settingsFragment
    )

    private var navController: NavController? = null

    private var mPath: Path? = null
    private var mPaint: Paint? = null

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

    private val builder = NavOptions.Builder()
        .setLaunchSingleTop(true)
//        .setEnterAnim(R.anim.nav_default_enter_anim)
//        .setExitAnim(R.anim.nav_default_exit_anim)
//        .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
//        .setPopExitAnim(R.anim.nav_default_pop_exit_anim)

    init {
        mPath = Path()
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint?.style = Paint.Style.FILL_AND_STROKE
        mPaint?.color = ContextCompat.getColor(getContext(), R.color.bottom_navigation_view_color)
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
        mPath!!.reset()
        mPath!!.moveTo(0f, (mNavigationBarHeight / 2).toFloat())
        mPath!!.cubicTo(
            0f,
            (CURVE_CIRCLE_RADIUS / 2).toFloat(),
            (CURVE_CIRCLE_RADIUS / 2).toFloat(),
            0f,
            CURVE_CIRCLE_RADIUS.toFloat(),
            0f
        )
        mPath!!.lineTo(mFirstCurveStartPoint.x.toFloat(), mFirstCurveStartPoint.y.toFloat())
        mPath!!.cubicTo(
            mFirstCurveControlPoint1.x.toFloat(), mFirstCurveControlPoint1.y.toFloat(),
            mFirstCurveControlPoint2.x.toFloat(), mFirstCurveControlPoint2.y.toFloat(),
            mFirstCurveEndPoint.x.toFloat(), mFirstCurveEndPoint.y.toFloat()
        )
        mPath!!.cubicTo(
            mSecondCurveControlPoint1.x.toFloat(), mSecondCurveControlPoint1.y.toFloat(),
            mSecondCurveControlPoint2.x.toFloat(), mSecondCurveControlPoint2.y.toFloat(),
            mSecondCurveEndPoint.x.toFloat(), mSecondCurveEndPoint.y.toFloat()
        )
        mPath!!.lineTo(mNavigationBarWidth - CURVE_CIRCLE_RADIUS.toFloat(), 0f)
        mPath!!.cubicTo(
            mNavigationBarWidth - (CURVE_CIRCLE_RADIUS / 2).toFloat(),
            0f,
            mNavigationBarWidth.toFloat(),
            (CURVE_CIRCLE_RADIUS / 2).toFloat(),
            mNavigationBarWidth.toFloat(),
            (mNavigationBarHeight / 2).toFloat()
        )
        mPath!!.lineTo(mNavigationBarWidth.toFloat(), mNavigationBarHeight.toFloat())
        mPath!!.lineTo(0f, mNavigationBarHeight.toFloat())
        mPath!!.close()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(mPath!!, mPaint!!)
    }

    fun navigate(index: Int) {
        navController?.let {

            if (it.currentDestination?.id != destinations[index]) {
                if (index != 2) {
                    builder.setPopUpTo(ids[2], false)

                    val options = builder.build()

                    try {
                        navController?.navigate(ids[index], null, options)
                    } catch (e: IllegalArgumentException) {

                    }
                } else {
                    it.popBackStack(ids[2], false)
                }
            } else {
                Timber.i("Reselection same page")
            }

        }
    }

    fun setupWithNavController(navController: NavController) {
        this.navController = navController
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewScope.cancel()
    }

}