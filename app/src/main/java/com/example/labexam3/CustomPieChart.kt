package com.example.labexam3

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color // Add this import import android.graphics.Paint
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.labexam3.R
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class ChartEntry(val label: String, val value: Float, val color: Int)

class CustomPieChart @JvmOverloads constructor( context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0 ) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = context.resources.getColor(R.color.text_secondary, null)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.resources.getColor(R.color.text_primary, null)
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }
    private val legendPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24f
        color = context.resources.getColor(R.color.text_primary, null)
    }
    private val oval = RectF()
    private var entries: List<ChartEntry> = emptyList()
    private var selectedIndex: Int = -1
    private val colors = listOf(
        Color.parseColor("#FF6F61"),
        Color.parseColor("#6B5B95"),
        Color.parseColor("#88B04B"),
        Color.parseColor("#F7CAC9"),
        Color.parseColor("#92A8D1")
    )

    fun getEntries(): List<ChartEntry> = entries

    fun setData(entries: List<ChartEntry>) {
        this.entries = entries.mapIndexed { index, entry ->
            ChartEntry(entry.label, entry.value, colors[index % colors.size])
        }
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val total = entries.sumOf { it.value.toDouble() }.toFloat()
        if (total == 0f) return

        val centerX = width / 2f
        val centerY = height / 2f - 50f // Shift up to make space for legend
        val radius = minOf(width, height) / 2f * 0.7f

        oval.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        var startAngle = -90f // Start from top
        entries.forEachIndexed { index, entry ->
            val sweepAngle = (entry.value / total) * 360f
            paint.color = entry.color
            if (index == selectedIndex) {
                // Slightly scale the selected segment
                val scale = 1.05f
                val scaledOval = RectF(
                    centerX - radius * scale,
                    centerY - radius * scale,
                    centerX + radius * scale,
                    centerY + radius * scale
                )
                canvas.drawArc(scaledOval, startAngle, sweepAngle, true, paint)
                canvas.drawArc(scaledOval, startAngle, sweepAngle, true, strokePaint)
            } else {
                canvas.drawArc(oval, startAngle, sweepAngle, true, paint)
                canvas.drawArc(oval, startAngle, sweepAngle, true, strokePaint)
            }

            // Draw percentage/value on the segment
            if (entry.value > 0) {
                val percentage = (entry.value / total * 100).toInt()
                val angle = startAngle + sweepAngle / 2
                val rad = Math.toRadians(angle.toDouble())
                val labelX = (centerX + radius * 0.5f * cos(rad)).toFloat()
                val labelY = (centerY + radius * 0.5f * sin(rad)).toFloat() + valuePaint.textSize / 3
                if (percentage >= 5) { // Only show percentage if significant
                    canvas.drawText("$percentage%", labelX, labelY, valuePaint)
                }
            }

            startAngle += sweepAngle
        }

        // Draw center hole
        paint.color = context.resources.getColor(R.color.transaction_card_background_color, null)
        canvas.drawCircle(centerX, centerY, radius * 0.3f, paint)

        // Draw legend at the bottom
        val legendTop = centerY + radius + 50f
        val legendSpacing = 40f
        entries.forEachIndexed { index, entry ->
            val legendX = 40f
            val legendY = legendTop + index * legendSpacing
            paint.color = entry.color
            canvas.drawCircle(legendX, legendY, 10f, paint)
            canvas.drawText("${entry.label}: Rs. ${entry.value.toInt()}", legendX + 30f, legendY + 8f, legendPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y
                val centerX = width / 2f
                val centerY = height / 2f - 50f
                val radius = minOf(width, height) / 2f * 0.7f

                // Check if touch is within the pie chart
                val dx = x - centerX
                val dy = y - centerY
                val distance = Math.sqrt((dx * dx + dy * dy).toDouble())
                if (distance <= radius && distance >= radius * 0.3f) {
                    // Calculate the angle of the touch
                    val angle = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()
                    var normalizedAngle = if (angle < 0) angle + 360 else angle
                    normalizedAngle = (normalizedAngle + 90) % 360 // Adjust for starting from top

                    // Determine which segment was touched
                    val total = entries.sumOf { it.value.toDouble() }.toFloat()
                    var startAngle = 0f
                    entries.forEachIndexed { index, entry ->
                        val sweepAngle = (entry.value / total) * 360f
                        if (normalizedAngle >= startAngle && normalizedAngle < startAngle + sweepAngle) {
                            selectedIndex = index
                            invalidate()
                            return true
                        }
                        startAngle += sweepAngle
                    }
                }
                selectedIndex = -1
                invalidate()
            }
        }
        return true
    }
}