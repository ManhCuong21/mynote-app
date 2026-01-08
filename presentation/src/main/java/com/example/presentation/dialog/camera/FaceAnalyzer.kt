package com.example.presentation.dialog.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import timber.log.Timber
import java.io.ByteArrayOutputStream

@ExperimentalGetImage
class FaceAnalyzer(
    private val cascadeClassifier: CascadeClassifier?,
    private val onFacesDetected: (faces: List<Rect>, width: Int, height: Int) -> Unit
) : ImageAnalysis.Analyzer {

    @Volatile
    private var isProcessing = false
    private var lastAnalyzedTime = 0L

    override fun analyze(imageProxy: ImageProxy) {
        if (isProcessing) {
            return
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalyzedTime < 120) { // ~8fps
            return
        }
        lastAnalyzedTime = currentTime
        isProcessing = true // Đặt cờ xử lý

        try {
            val mediaImage = imageProxy.image ?: return
            val bitmap =
                mediaImage.toBitmap().rotate(imageProxy.imageInfo.rotationDegrees.toFloat())

            val frame = Mat()
            Utils.bitmapToMat(bitmap, frame)
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB)

            smoothSkinFast(frame)

            val grayMat = Mat()
            Imgproc.cvtColor(frame, grayMat, Imgproc.COLOR_RGB2GRAY)
            val faces = mutableListOf<Rect>()
            cascadeClassifier?.let { classifier ->
                val detectedFaces = MatOfRect()
                classifier.detectMultiScale(
                    grayMat,
                    detectedFaces,
                    1.1,
                    3,
                    0,
                    Size(60.0, 60.0),
                    Size()
                )
                faces.addAll(
                    detectedFaces.toArray().map {
                        Rect(it.x, it.y, it.x + it.width, it.y + it.height)
                    }
                )
                detectedFaces.release()
            }

            grayMat.release()
            frame.release()

            onFacesDetected(faces, bitmap.width, bitmap.height)
        } catch (e: Exception) {
            Timber.e(e, "FaceAnalyzer error")
        } finally {
            isProcessing = false
        }
    }
}

private fun Image.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 90, out)
    val bytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

private fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

private fun smoothSkinFast(inputMat: Mat): Mat {
    val result = Mat()
    Imgproc.bilateralFilter(inputMat, result, 9, 75.0, 75.0)

    val blurred = Mat()
    Imgproc.GaussianBlur(result, blurred, Size(3.0, 3.0), 0.0)

    Core.addWeighted(result, 0.7, blurred, 0.3, 0.0, result)

    blurred.release()
    return result
}