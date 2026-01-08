package com.example.presentation.dialog.camera

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class FaceOverlayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private var faces: List<Rect> = emptyList()
    private var imageWidth = 0
    private var imageHeight = 0
    private var isFrontCamera = false

    fun setFaces(faces: List<Rect>, imgWidth: Int, imgHeight: Int, isFront: Boolean) {
        this.faces = faces
        this.imageWidth = imgWidth
        this.imageHeight = imgHeight
        this.isFrontCamera = isFront
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (imageWidth == 0 || imageHeight == 0) return

        val scaleX = width.toFloat() / imageWidth
        val scaleY = height.toFloat() / imageHeight

        faces.forEach { rect ->
            val left = rect.left * scaleX
            val top = rect.top * scaleY
            val right = rect.right * scaleX
            val bottom = rect.bottom * scaleY

            if (isFrontCamera) {
                // Lật gương (mirror)
                val mirroredLeft = width - right
                val mirroredRight = width - left
                canvas.drawRect(mirroredLeft, top, mirroredRight, bottom, paint)
            } else {
                canvas.drawRect(left, top, right, bottom, paint)
            }
        }
    }
}