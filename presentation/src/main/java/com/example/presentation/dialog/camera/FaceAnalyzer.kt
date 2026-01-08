package com.example.presentation.dialog.camera

import android.graphics.Rect
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import timber.log.Timber

@ExperimentalGetImage
class FaceAnalyzer(
    private val onFacesDetected: (faces: List<Rect>, width: Int, height: Int, rotation: Int) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .enableTracking() // giúp box mượt hơn
            .build()

        FaceDetection.getClient(options)
    }

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                val boundingBoxes = faces.map { it.boundingBox }
                // Gửi kích thước của ảnh gốc từ cảm biến và góc quay
                onFacesDetected(
                    boundingBoxes,
                    imageProxy.width,
                    imageProxy.height,
                    rotationDegrees
                )
            }
            .addOnFailureListener { Timber.e(it) }
            .addOnCompleteListener { imageProxy.close() }
    }
}