package com.example.presentation.dialog.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
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
import java.io.File
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

@AndroidEntryPoint
class CameraDialogFragment : DialogFragment() {

    @Inject
    lateinit var fileUseCase: FileUseCase

    private var builder: Builder? = null
    private lateinit var binding: FragmentCameraDialogBinding


    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService
    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_AlertDialogFullScreen)
        imageCapture = ImageCapture.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build()
        val orientationEventListener = object : OrientationEventListener(requireActivity()) {
            override fun onOrientationChanged(orientation: Int) {
                // Monitors orientation values to determine the target rotation value
                val rotation: Int = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageCapture!!.targetRotation = rotation
            }
        }
        orientationEventListener.enable()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = layoutInflater.inflateViewBinding(
            parent = container,
            attachToParent = false
        )
        startCamera()
        // Set up the listeners for take photo and video capture buttons
        binding.imageCaptureButton.setOnClickListener { takePhoto() }

        cameraExecutor = Executors.newSingleThreadExecutor()

        return binding.root
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val fileName = formatDate(FILE_NAME_FORMAT)
        val file = File(
            fileUseCase.getOutputMediaDirectoryTemp(fragmentActivity = requireActivity()),
            "$fileName.jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(file)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireActivity()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    exc.printStackTrace()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    builder?.let {
                        it.takePictureClickListener()
                    }
                    dismiss()
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
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
