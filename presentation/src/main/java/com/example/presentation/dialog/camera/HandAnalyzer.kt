package com.example.presentation.dialog.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker.HandLandmarkerOptions
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import timber.log.Timber
import kotlin.math.abs

class HandAnalyzer(
    context: Context,
    private val onHandDetected: () -> Unit
) : ImageAnalysis.Analyzer {

    private val landmarker: HandLandmarker
    private var lastDetectionTime = 0L
    private val debounceIntervalMs = 500L

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val options = HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumHands(1)
            .setResultListener { result, _ -> // ✅ Sửa: Bỏ qua inputImage (dùng _)
                // ✅ Sửa: Chỉ gọi hàm với 1 tham số, tránh lỗi "No value passed for parameter 'timestamp'."
                handleLandmarkerResult(result)
            }
            .setErrorListener { error ->
                Timber.e(error, "HandLandmarker Error")
            }
            .build()

        landmarker = HandLandmarker.createFromOptions(context, options)
    }

    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()

        try {
            // --- 1. Xử lý Debounce ---
            if (currentTime - lastDetectionTime < debounceIntervalMs) {
                // Nếu đang trong cooldown, ta không cần chạy phân tích MediaPipe.
                // Bỏ qua và đóng ImageProxy.
                return
            }

            // --- 2. Xử lý Ảnh (chỉ chạy khi không bị debounce) ---

            // Dòng này cần được thực thi thành công
            val bitmap = imageProxy.toBitmap()
            val rotatedBitmap = bitmap.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            val mpImage: MPImage = BitmapImageBuilder(rotatedBitmap).build()

            // Gửi frame đi. KHI SỬ DỤNG LIVE_STREAM, CameraX VẪN YÊU CẦU BẠN ĐÓNG
            landmarker.detectAsync(mpImage, currentTime)

        } catch (e: Exception) {
            Timber.e(e, "Error processing image for hand detection")
            // Giữ shouldClose = true để đóng ImageProxy nếu có lỗi xảy ra trước khi MediaPipe kịp làm gì.
        } finally {}
    }
    private fun handleLandmarkerResult(result: HandLandmarkerResult) {
        if (result.handednesses().isNotEmpty()) {
            val handLandmarks = result.landmarks()[0]
            if (isOpenPalm(handLandmarks)) {
                Timber.d("HAND DETECTED: Triggering takePhoto()") // ✅ Thêm dòng này
                onHandDetected()
                lastDetectionTime = System.currentTimeMillis()
            }
        }
    }

    // Trong HandAnalyzer.kt

    // Logic phát hiện bàn tay mở (Đã sửa đổi để nhạy hơn)
    private fun isOpenPalm(landmarks: List<NormalizedLandmark>): Boolean {
        // Chỉ số các điểm quan trọng:
        // 0: Cổ tay (Wrist)
        // 5: Gốc ngón trỏ (Index MCP)
        // 17: Gốc ngón út (Pinky MCP)
        // 8: Đầu ngón trỏ (Index Tip)
        // 20: Đầu ngón út (Pinky Tip)

        // 1. Kiểm tra Lòng bàn tay Mở: Các đầu ngón tay phải cao hơn khớp gốc
        fun isFingerStraight(tipIndex: Int, mcpIndex: Int): Boolean {
            // Tip.y < MCP.y nghĩa là ngón tay duỗi thẳng (y-axis tăng từ trên xuống)
            return landmarks[tipIndex].y() < landmarks[mcpIndex].y()
        }

        val isIndexStraight = isFingerStraight(8, 5)
        val isPinkyStraight = isFingerStraight(20, 17)

        // 2. Kiểm tra độ mở rộng của bàn tay (để phân biệt nắm tay lỏng và bàn tay mở)
        // Tính khoảng cách giữa gốc ngón trỏ (5) và gốc ngón út (17) (khoảng cách trên trục X)
        val horizontalDistance = abs(landmarks[5].x() - landmarks[17].x())

        // 3. Kiểm tra khoảng cách cổ tay - ngón tay (chiều dài bàn tay)
//        val wristToFingerLength = abs(landmarks[0].y() - landmarks[8].y())

        // Nếu bàn tay rất gần camera, giá trị khoảng cách có thể lớn hơn 0.1.
        // Thử nghiệm với các ngưỡng đơn giản:
        val isWideEnough = horizontalDistance > 0.15f // Bàn tay phải mở rộng
//        val isLongEnough = wristToFingerLength > 0.2f // Đảm bảo đó là bàn tay đầy đủ

        // Cử chỉ kích hoạt chụp ảnh:
        // Ít nhất ngón trỏ hoặc ngón út phải duỗi thẳng VÀ bàn tay phải mở đủ rộng.
        val isGestureDetected = (isIndexStraight || isPinkyStraight) && isWideEnough

        return isGestureDetected
    }
}

private fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}