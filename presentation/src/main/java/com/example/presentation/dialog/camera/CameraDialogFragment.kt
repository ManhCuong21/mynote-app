package com.example.presentation.dialog.camera

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.core.core.external.AppConstants.FILE_NAME_FORMAT
import com.example.core.core.external.formatDate
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.domain.usecase.file.FileUseCase
import com.example.presentation.R
import com.example.presentation.databinding.FragmentCameraDialogBinding
import dagger.hilt.android.AndroidEntryPoint
import org.opencv.objdetect.CascadeClassifier
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject


fun Fragment.showCameraDialog(
    tag: String = this::class.java.simpleName,
    init: CameraDialogFragment.Builder.() -> Unit,
) {
    val builder = CameraDialogFragment.Builder().apply(init)
    CameraDialogFragment.getInstance(builder)
        .show(
            requireActivity().supportFragmentManager,
            "${CameraDialogFragment.CAMERA_DIALOG_FRAGMENT_TAG}.$tag"
        )
}

@ExperimentalGetImage
@AndroidEntryPoint
class CameraDialogFragment : DialogFragment() {

    @Inject
    lateinit var fileUseCase: FileUseCase

    private var builder: Builder? = null
    private lateinit var binding: FragmentCameraDialogBinding
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var cameraProvider: ProcessCameraProvider? = null
    private var isFlashOn = false
    private var cascadeClassifier: CascadeClassifier? = null
    private var handCascadeClassifier: CascadeClassifier? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService
    private val handler = Handler(Looper.getMainLooper())
    private val autoCaptureDelayMs = 3000L
    private var isCountingDown = false
    private var startTime: Long = 0L
    private val countdownUpdateRunnable = object : Runnable {
        override fun run() {
            // 💥 KIỂM TRA TRẠNG THÁI TRƯỚC KHI TRUY CẬP UI 💥
            if (!isCountingDown || !isAdded || view == null) return

            val elapsed = System.currentTimeMillis() - startTime
            val remainingSeconds = 3 - (elapsed / 1000).toInt()

            if (remainingSeconds >= 1) {
                // KHÔNG CẦN activity?.runOnUiThread NỮA vì Handler là Main Looper
                binding.countdownTextView.text = remainingSeconds.toString()
                handler.postDelayed(this, 1000L)
            }
        }
    }

    // 1. Runnable gốc: Chạy sau 3 giây để chụp ảnh
    private val autoCaptureRunnable = Runnable {
        // 💥 KIỂM TRA TRẠNG THÁI TRƯỚC KHI TRUY CẬP UI 💥
        if (!isAdded || view == null) return@Runnable

        isCountingDown = false
        handler.removeCallbacks(countdownUpdateRunnable)
        binding.countdownTextView.visibility = View.GONE
        takePhoto()
    }

    // 3. Cải tiến hàm startCountdown
    private fun startCountdown() {
        // Luôn đảm bảo an toàn khi gọi từ luồng nền (HandAnalyzer)
        if (!isAdded || view == null) return

        // ... (logic removeCallbacks và thiết lập trạng thái như cũ)

        handler.removeCallbacks(autoCaptureRunnable)
        handler.removeCallbacks(countdownUpdateRunnable)

        isCountingDown = true
        Timber.d("Starting 3s countdown...")

        // Reset thời gian và hiển thị/reset View
        binding.countdownTextView.text = "3"
        binding.countdownTextView.visibility = View.VISIBLE
        startTime = System.currentTimeMillis()

        // Bắt đầu cập nhật UI mỗi giây
        handler.post(countdownUpdateRunnable)

        // Post Runnable chụp ảnh (sẽ chạy chính xác sau 3000ms)
        handler.postDelayed(autoCaptureRunnable, autoCaptureDelayMs)
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_AlertDialogFullScreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = layoutInflater.inflateViewBinding(container, false)

        // Fix scale camera not zooming in
        binding.previewView.scaleType = PreviewView.ScaleType.FIT_CENTER
        try {
            System.loadLibrary("opencv_java4")
            loadCascadeFile()
            Timber.d("Loaded OpenCV native libs")
        } catch (e: UnsatisfiedLinkError) {
            Timber.e("Failed to load OpenCV libs: + $e")
        }

        cameraExecutor = Executors.newFixedThreadPool(2)
        startCamera()
        setupClickListener()
        return binding.root
    }

    private fun loadCascadeFile() {
        try {
            val faceInput = requireContext().assets.open("haarcascade_frontalface_default.xml")
            val cascadeDir = requireContext().getDir("cascade", 0)

            val faceFile = File(cascadeDir, "haarcascade_frontalface_default.xml")
            faceInput.copyTo(FileOutputStream(faceFile))
            faceInput.close()
            cascadeClassifier = CascadeClassifier(faceFile.absolutePath)
            if (cascadeClassifier!!.empty()) cascadeClassifier = null

            // Load hand cascade
            val handInput = requireContext().assets.open("haarcascade_hand.xml")
            val handFile = File(cascadeDir, "haarcascade_hand.xml")
            handInput.copyTo(FileOutputStream(handFile))
            handInput.close()
            handCascadeClassifier = CascadeClassifier(handFile.absolutePath)
            if (handCascadeClassifier!!.empty()) handCascadeClassifier = null

            Timber.d("Cascade loaded: face=${cascadeClassifier != null}, hand=${handCascadeClassifier != null}")
        } catch (e: Exception) {
            Timber.e("Error loading cascade: $e")
        }
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider = provider
            provider.unbindAll()

            val resolutionSelector = ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                .build()

            // --- Preview ---
            val preview = Preview.Builder()
                .setResolutionSelector(resolutionSelector)
                .build()
                .also {
                    it.surfaceProvider = binding.previewView.surfaceProvider
                }

            // --- ImageCapture ---
            imageCapture = ImageCapture.Builder()
                .setResolutionSelector(resolutionSelector)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            imageAnalyzer = ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    val handAnalyzer = HandAnalyzer(requireContext()) {
                        if (!isCountingDown) {
                            isCountingDown = true
                            Timber.d("Hand Detected: Starting 3s countdown...")
                            requireActivity().runOnUiThread {
                                startCountdown()
                            }
                        }
                    }

                    val faceAnalyzerImpl = FaceAnalyzer(cascadeClassifier) { faces, imgW, imgH ->
                        if (!isAdded || activity == null) return@FaceAnalyzer
                        activity?.runOnUiThread {
                            binding.faceOverlayView.setFaces(
                                faces,
                                imgW,
                                imgH,
                                cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
                            )
                        }
                    }

                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        try {
                            // 1. Face Analysis (DO NOT close)
                            faceAnalyzerImpl.analyze(imageProxy)

                            // 2. Hand Analysis (NOT closed)
                            handAnalyzer.analyze(imageProxy)

                        } catch (e: Exception) {
                            // If there is an error, we still try to close the file, but the finally block does it better.
                            e.printStackTrace()
                        } finally {
                            // IMPORTANT STEP: CLOSE ONLY AND MANDATORY HERE
                            // This frees the buffer for CameraX to feed the next frame.
                            try {
                                imageProxy.close()
                            } catch (e: Exception) {
                                Timber.e("Error closing image in CameraDialogFragment: $e")
                            }
                        }
                    }
                }

            // --- Bind use cases ---
            camera = provider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalyzer
            )

            // --- Reset zoom to 1.0 to avoid zooming ---
            camera?.cameraControl?.setZoomRatio(1.0f)

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun setupClickListener() = binding.apply {
        btnSwitchCamera.setOnClickListener { switchCamera() }
        btnFlash.setOnClickListener { toggleFlash() }
        btnCapture.setOnCaptureListener { takePhoto() }
    }

    private fun toggleFlash() {
        val cam = camera ?: return
        isFlashOn = !isFlashOn
        val iconFlash = if (isFlashOn) R.drawable.icon_flash_on_24 else R.drawable.icon_flash_off_24
        binding.btnFlash.setImageResource(iconFlash)
        cam.cameraControl.enableTorch(isFlashOn)
    }


    private fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        binding.btnFlash.isEnabled = cameraSelector != CameraSelector.DEFAULT_FRONT_CAMERA
        startCamera() // rebind camera mới
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val fileName = formatDate(FILE_NAME_FORMAT)
        val file = File(
            fileUseCase.createDirectoryTemp(),
            "$fileName.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireActivity()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Timber.e(exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    builder?.takePictureClickListener?.invoke()
                    dismissAllowingStateLoss()
                    releaseCamera()
                }
            }
        )
    }

    private fun releaseCamera() {
        cameraProvider?.unbindAll()
        imageAnalyzer?.clearAnalyzer()
        cameraExecutor.shutdownNow()
    }

    override fun onPause() {
        super.onPause()
        imageAnalyzer?.clearAnalyzer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseCamera()
        handler.removeCallbacks(autoCaptureRunnable)
        handler.removeCallbacks(countdownUpdateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!cameraExecutor.isShutdown) cameraExecutor.shutdown()
    }

    class Builder {
        internal var takePictureClickListener: () -> Unit = { }
            private set

        fun takePictureAction(
            listener: () -> Unit,
        ) {
            takePictureClickListener = listener
        }
    }

    companion object {
        fun getInstance(builder: Builder): CameraDialogFragment {
            return CameraDialogFragment().apply { this.builder = builder }
        }

        const val CAMERA_DIALOG_FRAGMENT_TAG = "CameraDialogFragment"
    }
}