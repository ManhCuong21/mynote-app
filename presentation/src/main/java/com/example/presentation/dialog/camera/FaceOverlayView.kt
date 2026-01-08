package com.example.presentation.dialog.camera

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class FaceOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 8f // Tăng độ dày cho dễ nhìn
    }

    private var faces: List<Rect> = emptyList()
    private var imageWidth = 0
    private var imageHeight = 0
    private var isFrontCamera = true
    private var rotationDegrees = 0

    // Cập nhật hàm setFaces trong FaceOverlayView.kt
    fun setFaces(
        faces: List<Rect>,
        imgWidth: Int,
        imgHeight: Int,
        isFront: Boolean,
        rotation: Int
    ) {
        this.faces = faces
        this.imageWidth = imgWidth
        this.imageHeight = imgHeight
        this.isFrontCamera = isFront
        this.rotationDegrees = rotation // Lưu lại rotation
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (faces.isEmpty() || imageWidth == 0 || imageHeight == 0) return

        // Quan trọng: Nếu rotation là 90 hoặc 270, sensor đang ở chế độ Portrait
        // Chiều rộng của ảnh sensor lúc này thực chất là chiều cao trên màn hình và ngược lại
        val isRotated = rotationDegrees == 90 || rotationDegrees == 270
        val effectiveWidth = if (isRotated) imageHeight else imageWidth
        val effectiveHeight = if (isRotated) imageWidth else imageHeight

        val scaleX = width.toFloat() / effectiveWidth
        val scaleY = height.toFloat() / effectiveHeight

        faces.forEach { rect ->
            // Chuyển đổi tọa độ dựa trên scale
            var left = rect.left * scaleX
            val top = rect.top * scaleY
            var right = rect.right * scaleX
            val bottom = rect.bottom * scaleY

            // Xử lý lật gương cho camera trước
            if (isFrontCamera) {
                val mirroredLeft = width - right
                val mirroredRight = width - left
                left = mirroredLeft
                right = mirroredRight
            }

            canvas.drawRect(left, top, right, bottom, paint)
        }
    }
}