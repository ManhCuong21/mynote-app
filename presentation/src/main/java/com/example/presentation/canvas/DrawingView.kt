package com.example.presentation.canvas

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.createBitmap

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private lateinit var drawingBitmap: Bitmap
    private lateinit var drawingCanvas: Canvas

    private var currentPath = Path()
    private var currentPaint = Paint()
    private var drawColor = Color.BLACK
    private var strokeWidth = 10f
    private var isEraserMode = false

    init {
        setupPaint()
    }

    private fun setupPaint() {
        currentPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            this.strokeWidth = this@DrawingView.strokeWidth
            if (isEraserMode) {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            } else {
                xfermode = null
                color = drawColor
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawingBitmap = createBitmap(w, h)
        drawingCanvas = Canvas(drawingBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(drawingBitmap, 0f, 0f, null) // nét đã vẽ
        canvas.drawPath(currentPath, currentPaint)     // nét đang vẽ
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath.moveTo(x, y)
            }

            MotionEvent.ACTION_MOVE -> {
                currentPath.lineTo(x, y)
                if (isEraserMode) {
                    // Tẩy ngay khi di chuyển
                    drawingCanvas.drawPath(currentPath, currentPaint)
                    currentPath.reset()
                    currentPath.moveTo(x, y)
                }
            }

            MotionEvent.ACTION_UP -> {
                if (!isEraserMode) {
                    drawingCanvas.drawPath(currentPath, currentPaint)
                }
                currentPath.reset()
            }
        }

        invalidate()
        return true
    }

    fun setColor(color: Int) {
        drawColor = color
        isEraserMode = false
        setupPaint()
    }

    fun setStrokeWidth(width: Float) {
        strokeWidth = width
        setupPaint()
    }

    fun enableEraser(enabled: Boolean) {
        isEraserMode = enabled
        setupPaint()
    }

    fun clearCanvas() {
        drawingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    fun saveToBitmap(backgroundBitmap: Bitmap): Bitmap {
        val result = createBitmap(width, height)
        val canvas = Canvas(result)

        // Vẽ ảnh nền trước
        canvas.drawBitmap(backgroundBitmap, null, Rect(0, 0, width, height), null)
        // Vẽ nét đã vẽ
        canvas.drawBitmap(drawingBitmap, 0f, 0f, null)

        return result
    }
}
