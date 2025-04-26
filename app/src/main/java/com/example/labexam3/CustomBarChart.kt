package com.example.labexam3

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.labexam3.R

data class BarChartEntry(val label: String, val value: Float, val color: Int)

class CustomBarChart @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.resources.getColor(R.color.text_primary, null)
        textSize = 32f
        textAlign = Paint.Align.CENTER
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.resources.getColor(R.color.text_secondary, null)
        textSize = 24f
        textAlign = Paint.Align.RIGHT
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.resources.getColor(R.color.text_primary, null)
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }
    private var entries: List<BarChartEntry> = emptyList()

    fun setData(entries: List<BarChartEntry>) {
        this.entries = entries
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (entries.isEmpty()) return

        val maxValue = entries.maxOf { it.value.coerceAtLeast(0f) }
        val minValue = entries.minOf { it.value.coerceAtMost(0f) }
        val valueRange = maxValue - minValue
        if (valueRange == 0f) return

        val padding = 60f // Increased padding for Y-axis labels
        val barWidth = (width - padding * 2) / entries.size.toFloat() * 0.4f
        val spacing = (width - padding * 2) / entries.size.toFloat() * 0.2f
        val chartHeight = height - padding * 2

        // Draw Y-axis
        canvas.drawLine(padding, padding, padding, height - padding, axisPaint)

        // Draw zero line and Y-axis labels
        val zeroY = if (minValue < 0) {
            height - padding - (chartHeight * (maxValue / valueRange))
        } else {
            height - padding
        }
        paint.color = context.resources.getColor(R.color.text_secondary, null)
        paint.strokeWidth = 2f
        canvas.drawLine(padding, zeroY, width - padding, zeroY, paint)

        // Y-axis labels (5 steps)
        val steps = 5
        val stepValue = valueRange / steps
        for (i in 0..steps) {
            val value = minValue + (stepValue * i)
            val y = height - padding - (chartHeight * ((value - minValue) / valueRange))
            canvas.drawText(value.toInt().toString(), padding - 10f, y + axisPaint.textSize / 3, axisPaint)
        }

        entries.forEachIndexed { index, entry ->
            val x = padding + index * (barWidth + spacing) + spacing / 2
            val barHeight = (entry.value / valueRange) * chartHeight
            val topY = if (entry.value >= 0) {
                zeroY - barHeight
            } else {
                zeroY
            }
            val bottomY = if (entry.value >= 0) {
                zeroY
            } else {
                zeroY - barHeight
            }

            paint.color = entry.color
            canvas.drawRect(x, topY, x + barWidth, bottomY, paint)

            // Draw value above the bar
            val valueY = if (entry.value >= 0) topY - 10f else bottomY + 30f
            canvas.drawText(entry.value.toInt().toString(), x + barWidth / 2, valueY, valuePaint)

            // Draw label below the bar
            val labelX = x + barWidth / 2
            val labelY = height - padding / 2 + 20f
            canvas.save()
            canvas.rotate(-45f, labelX, labelY)
            canvas.drawText(entry.label, labelX, labelY, textPaint)
            canvas.restore()
        }
    }
}