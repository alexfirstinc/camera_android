package com.example.gua

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class BaguaView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val ringNumbers = listOf(9, 4, 3, 8, 1, 6, 7, 2) // clockwise from South

    private val sectorColors = listOf(
        Color.parseColor("#EF5350"),
        Color.parseColor("#FF9800"),
        Color.parseColor("#FDD835"),
        Color.parseColor("#66BB6A"),
        Color.parseColor("#26C6DA"),
        Color.parseColor("#42A5F5"),
        Color.parseColor("#7E57C2"),
        Color.parseColor("#EC407A")
    )

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    private var heading = 0f
    private var userGua = -1

    fun setHeading(degreesTrueNorth: Float) {
        heading = degreesTrueNorth
        invalidate()
    }

    fun setUserGua(value: Int) {
        userGua = value
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) * 0.38f
        val inner = radius * 0.58f

        // Rotate octagon opposite to heading so it stays fixed against cardinal directions.
        canvas.save()
        canvas.rotate(-heading, cx, cy)

        for (i in 0 until 8) {
            val startDeg = -90f + i * 45f // North starts at top
            val endDeg = startDeg + 45f
            val path = sectorPath(cx, cy, radius, inner, startDeg, endDeg)
            fillPaint.color = sectorColors[i]
            canvas.drawPath(path, fillPaint)

            val mid = Math.toRadians((startDeg + 22.5).toDouble())
            val tx = cx + cos(mid).toFloat() * (radius + inner) / 2f
            val ty = cy + sin(mid).toFloat() * (radius + inner) / 2f

            // Keep text upright on screen while octagon rotates.
            canvas.save()
            canvas.rotate(heading, tx, ty)
            textPaint.color = if (ringNumbers[i] == userGua) Color.WHITE else Color.BLACK
            textPaint.textSize = if (ringNumbers[i] == userGua) 56f else 44f
            canvas.drawText(ringNumbers[i].toString(), tx, ty + 14f, textPaint)
            canvas.restore()
        }

        canvas.restore()
    }

    private fun sectorPath(cx: Float, cy: Float, outerR: Float, innerR: Float, startDeg: Float, endDeg: Float): Path {
        val path = Path()

        val s1 = Math.toRadians(startDeg.toDouble())
        val e1 = Math.toRadians(endDeg.toDouble())

        val x1 = cx + cos(s1).toFloat() * outerR
        val y1 = cy + sin(s1).toFloat() * outerR
        val x2 = cx + cos(e1).toFloat() * outerR
        val y2 = cy + sin(e1).toFloat() * outerR
        val x3 = cx + cos(e1).toFloat() * innerR
        val y3 = cy + sin(e1).toFloat() * innerR
        val x4 = cx + cos(s1).toFloat() * innerR
        val y4 = cy + sin(s1).toFloat() * innerR

        path.moveTo(x1, y1)
        path.lineTo(x2, y2)
        path.lineTo(x3, y3)
        path.lineTo(x4, y4)
        path.close()
        return path
    }
}
