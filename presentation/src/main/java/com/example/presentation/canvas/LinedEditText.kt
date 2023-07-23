package com.example.presentation.canvas

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.example.presentation.R

class LinedEditText(context: Context?, attrs: AttributeSet?) :
    AppCompatEditText(context!!, attrs) {
    private var attributes: TypedArray
    var colorLine: String? = null
    private val mRect: Rect
    private val mPaint: Paint

    override fun onDraw(canvas: Canvas) {
        val height = height
        val lineHeight: Int = lineHeight
        var count = height / lineHeight
        if (lineCount > count) count = lineCount
        val r: Rect = mRect
        val paint: Paint = mPaint
        var baseline: Int = getLineBounds(0, r)
        for (i in 0 until count) {
            canvas.drawLine(
                r.left.toFloat(), (baseline + 5).toFloat(),
                r.right.toFloat(), (baseline + 5).toFloat(), paint
            )
            baseline += lineHeight
        }
        mPaint.color = Color.parseColor(colorLine)
        invalidate()
        super.onDraw(canvas)
    }

    init {
        attributes = context!!.obtainStyledAttributes(attrs, R.styleable.LinedEditText)
        colorLine = attributes.getString(R.styleable.LinedEditText_color_line)
        attributes.recycle()
        mRect = Rect()
        mPaint = Paint()
        mPaint.style = Paint.Style.STROKE
    }
}