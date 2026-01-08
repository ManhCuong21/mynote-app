package com.example.presentation.dialog.camera

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.example.presentation.R
import androidx.core.graphics.createBitmap

class CaptureButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Kích thước mặc định (dp)
        val defaultSizeDp = 100
        val defaultSizePx = (defaultSizeDp * resources.displayMetrics.density).toInt()

        // Lấy kích thước do parent gợi ý
        val width = resolveSize(defaultSizePx, widthMeasureSpec)
        val height = resolveSize(defaultSizePx, heightMeasureSpec)

        // Giữ hình vuông (vì là nút tròn)
        val size = minOf(width, height)

        setMeasuredDimension(size, size)
    }

    private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
    }

    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private var scaleFactor = 1f
    private var animator: ValueAnimator? = null
    private var onCaptureListener: (() -> Unit)? = null

    // --- Tuỳ chỉnh ---
    var outerStrokeWidth = 10f
        set(value) {
            field = value
            outerPaint.strokeWidth = value
            invalidate()
        }

    var outerRatio = 0.8f
    var innerRatio = 0.65f
    var iconRatio = 0.35f

    // --- Icon camera mặc định (VectorDrawable to Bitmap) ---
    private val cameraIcon: Bitmap by lazy {
        val drawable = AppCompatResources.getDrawable(context, R.drawable.baseline_photo_camera_24)
        drawable?.toBitmap() ?: createBitmap(1, 1)
    }

    fun setOnCaptureListener(listener: () -> Unit) {
        onCaptureListener = listener
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val baseRadius = minOf(width, height) / 2f

        // --- Vẽ vòng ngoài ---
        val outerRadius = baseRadius * outerRatio
        outerPaint.strokeWidth = outerStrokeWidth
        canvas.drawCircle(cx, cy, outerRadius, outerPaint)

        // --- Vẽ vòng trong (có scale khi nhấn) ---
        val innerRadius = baseRadius * innerRatio * scaleFactor
        canvas.drawCircle(cx, cy, innerRadius, innerPaint)

        // --- Vẽ icon camera ở chính giữa ---
        val iconSize = maxOf(baseRadius * iconRatio, 24f) // đảm bảo không quá nhỏ
        val left = cx - iconSize / 2
        val top = cy - iconSize / 2
        val rect = RectF(left, top, left + iconSize, top + iconSize)
        canvas.drawBitmap(cameraIcon, null, rect, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                animateScale(0.9f)
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                animateScale(1f)
                if (event.action == MotionEvent.ACTION_UP) {
                    onCaptureListener?.invoke()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun animateScale(target: Float) {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(scaleFactor, target).apply {
            duration = 150
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                scaleFactor = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }
}