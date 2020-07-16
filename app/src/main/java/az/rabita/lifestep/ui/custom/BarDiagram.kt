package az.rabita.lifestep.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import az.rabita.lifestep.R
import kotlinx.android.synthetic.main.layout_diagram.view.*

class BarDiagram @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val columnDistances = listOf(0.108f, 0.222f, 0.337f, 0.452f, 0.567f, 0.681f, 0.796f)
    private val checkingSpace = pxFromDP(10f)

    private val firstHorizontalGuidelineBias = 0.18f
    private val lastHorizontalGuidelineBias = 0.84f

    private val firstVerticalGuidelineBias = 0.06f
    private val lastVerticalGuidelineBias = 0.84f

    private var horizontalLineStartX: Float = 0f
    private var horizontalLineEndX: Float = 0f

    private var horizontalLineStartY: Float = 0f
    private var horizontalLineEndY: Float = 0f

    private lateinit var columns: List<TextView>
    private lateinit var rows: List<TextView>

    private var counter = 0

    private val paint = Paint()

    private val colorFirst = Color.parseColor("#353A85")
    private val colorSecond = Color.parseColor("#959AC2")
    private val colorGridLine = Color.parseColor("#E3E3E3")

    private var barLines: List<BarLine> = listOf()
    private var selectedBarLineIndex = -1

    init {
        post {
            inflate(context, R.layout.layout_diagram, this)
            initUI()
        }

        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = pxFromDP(5f)
    }

    private fun initUI() {
        columns = listOf(
            text_view_day1, text_view_day2, text_view_day3, text_view_day4, text_view_day5,
            text_view_day6, text_view_day7
        )

        rows = listOf(
            text_view_min_value, text_view_second_min_value,
            text_view_second_max_value, text_view_max_value
        )
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let { c ->

            if (barLines.isNotEmpty()) {

                val save = c.save()

                val spaceBetweenGridLines = (horizontalLineEndY - horizontalLineStartY) / 7f
                var horizontalLineLevel = horizontalLineStartY

                paint.color = colorGridLine
                paint.strokeWidth = pxFromDP(0.5f)

                repeat(8) {
                    c.drawLine(
                        horizontalLineStartX,
                        horizontalLineLevel,
                        horizontalLineEndX,
                        horizontalLineLevel,
                        paint
                    )
                    horizontalLineLevel += spaceBetweenGridLines
                }

                paint.strokeWidth = pxFromDP(5f)

                barLines.forEachIndexed { index, barLine ->

                    if (index == selectedBarLineIndex) {

                        paint.color = colorFirst

                        c.drawLine(
                            barLine.start.x, barLine.start.y,
                            barLine.end.x, barLine.end.y,
                            paint
                        )

                    } else {

                        if (barLine.bar.ratio > 0.21f) {

                            paint.color = colorFirst

                            c.drawLine(
                                barLine.start.x, barLine.start.y,
                                barLine.firstBreakPoint.x, barLine.firstBreakPoint.y,
                                paint
                            )

                            paint.color = colorSecond

                            c.drawLine(
                                barLine.secondBreakPoint.x, barLine.secondBreakPoint.y,
                                barLine.end.x, barLine.end.y,
                                paint
                            )

                        } else {

                            paint.color = colorFirst

                            c.drawLine(
                                barLine.start.x, barLine.start.y,
                                barLine.end.x, barLine.end.y,
                                paint
                            )

                        }

                    }

                }

                c.restoreToCount(save)

            }

        }

    }

    @SuppressLint("SetTextI18n")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event?.action == MotionEvent.ACTION_DOWN) {

            var flag = false

            for ((index, barLine) in barLines.withIndex()) {
                flag = barLine.isClicked(DrawPoint(event.x, event.y))
                if (flag) {
                    if (index != selectedBarLineIndex) {
                        selectedBarLineIndex = index
                        text_view_stat.text =
                            "${barLine.bar.value} ${context.getString(R.string.step)}"
                        invalidate()
                    }
                    break
                }
            }

            if (!flag) {
                selectedBarLineIndex = -1
                text_view_stat.text = ""
                invalidate()
            }

        }

        return true

    }

    fun submitData(data: DiagramDataModel) {
        post {
            if (selectedBarLineIndex != -1) {
                text_view_stat.text = ""
                selectedBarLineIndex = -1
            }
            prepareGridLines()
            processData(data) // await
            invalidate()
        }
    }

    private fun prepareGridLines() {
        horizontalLineStartX = width * firstVerticalGuidelineBias
        horizontalLineEndX = width * lastVerticalGuidelineBias

        horizontalLineStartY = height * firstHorizontalGuidelineBias
        horizontalLineEndY = height * lastHorizontalGuidelineBias
    }

    private fun processData(data: DiagramDataModel) {
        val rowsData = extractRowData(data.maxValue)

        applyDataToRows(rowsData) // can be asynchronous
        applyDataToColumns(data.columnTexts) // can be asynchronous

        val barsData = extractBarData(data.values, data.maxValue.toFloat())
        calculateCoordinates(barsData) // can be asynchronous
    }

    private fun extractBarData(data: List<Long>, maxValue: Float): MutableList<Bar> {
        val result = mutableListOf<Bar>()

        data.forEach { value -> result.add(Bar(value, value / maxValue)) }

        return result
    }

    private fun calculateCoordinates(data: List<Bar>) {
        val result = mutableListOf<BarLine>()

        data.forEachIndexed { index, bar ->

            val startPoint = DrawPoint()
            val endPoint = DrawPoint()

            startPoint.x = width * columnDistances[index]
            startPoint.y = height * 0.84f

            //Calculations end point and some other things which can be useful for parted line
            val distanceFromTopLine = height * (0.84f - 0.18f) * (1 - bar.ratio)
            val distanceFromBottomLine = height * (0.84 - 0.18f) * bar.ratio

            endPoint.x = startPoint.x
            endPoint.y = distanceFromTopLine + height * 0.18f

            val firstBreakPoint = DrawPoint()
            val secondBreakPoint = DrawPoint()

            firstBreakPoint.x = startPoint.x
            firstBreakPoint.y = endPoint.y + distanceFromBottomLine.toFloat() * 0.5f

            if (firstBreakPoint.y > startPoint.y) firstBreakPoint.y = startPoint.y

            secondBreakPoint.x = startPoint.x
            secondBreakPoint.y = firstBreakPoint.y - pxFromDP(10f)

            if (secondBreakPoint.y < endPoint.y) secondBreakPoint.y = endPoint.y

            val barLine =
                BarLine(bar, startPoint, endPoint, firstBreakPoint, secondBreakPoint, checkingSpace)

            result.add(barLine)

        }

        barLines = result
    }

    private fun extractRowData(maxValue: Long): List<String> {
        val result = mutableListOf<String>()
        val perRow = maxValue / 7
        var sum = perRow

        for (i in 0 until 4) {
            result.add(sum.toString())
            sum += (2 * perRow)
        }

        return result
    }

    private fun applyDataToRows(data: List<String>) {
        if (data.isNotEmpty()) rows.forEachIndexed { index, view -> view.text = data[index] }
    }

    private fun applyDataToColumns(data: List<String>) {
        if (data.isNotEmpty()) columns.forEachIndexed { index, view -> view.text = data[index] }
    }

    private fun pxFromDP(value: Float) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value,
        context.resources.displayMetrics
    )

    data class DiagramDataModel(
        val maxValue: Long,
        val values: List<Long>,
        val columnTexts: List<String>
    )

    data class Bar(val value: Long, val ratio: Float)

    data class DrawPoint(var x: Float = 0f, var y: Float = 0f)

    data class BarLine(
        val bar: Bar,
        val start: DrawPoint,
        val end: DrawPoint,
        val firstBreakPoint: DrawPoint,
        val secondBreakPoint: DrawPoint,
        val checkingSpace: Float
    ) {
        fun isClicked(clickedPoint: DrawPoint): Boolean {
            return ((clickedPoint.y >= end.y - checkingSpace && clickedPoint.y <= start.y + checkingSpace) &&
                    (clickedPoint.x >= start.x - checkingSpace && clickedPoint.x <= start.x + checkingSpace))
        }
    }

}