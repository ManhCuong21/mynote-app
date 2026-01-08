package com.example.presentation.main.setting.compass

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.CancellationSignal
import android.util.Log
import android.view.Display
import android.view.Surface
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.navigation.fragment.findNavController
import com.example.core.base.BaseFragment
import com.example.core.base.BaseViewModel
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.databinding.FragmentCompassBinding
import com.example.presentation.main.setting.compass.model.AppError
import com.example.presentation.main.setting.compass.model.Azimuth
import com.example.presentation.main.setting.compass.model.DisplayRotation
import com.example.presentation.main.setting.compass.model.RotationVector
import com.example.presentation.main.setting.compass.util.MathUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

const val OPTION_INSTRUMENTED_TEST = "INSTRUMENTED_TEST"

private const val TAG = "CompassFragment"

@AndroidEntryPoint
class CompassFragment : BaseFragment(R.layout.fragment_compass) {
    private val compassSensorEventListener = CompassSensorEventListener()
    override val binding: FragmentCompassBinding by viewBinding()
    override val viewModel: BaseViewModel
        get() = TODO("Not yet implemented")
    private var sensorManager: SensorManager? = null
    private var locationManager: LocationManager? = null
    private var locationRequestCancellationSignal: CancellationSignal? = null

    private fun setupSystemServices() {
        sensorManager = ActivityCompat.getSystemService(requireContext(), SensorManager::class.java)
        locationManager =
            ActivityCompat.getSystemService(requireContext(), LocationManager::class.java)
    }

    private fun adjustLayoutToSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
                bottomMargin = insets.bottom
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onResume() {
        super.onResume()

        if (isInstrumentedTest()) {
            Log.i(TAG, "Skipping start of system service functionalities")
        } else {
            startSystemServiceFunctionalities()
        }

        Log.i(TAG, "Started compass")
    }

    private fun isInstrumentedTest() =
        requireActivity().intent.extras?.getBoolean(OPTION_INSTRUMENTED_TEST) == true

    private fun startSystemServiceFunctionalities() {
        registerSensorListener()
    }

    private fun registerSensorListener() {
        sensorManager
            ?.also(::registerSensorListener)
            ?: run {
                Log.w(TAG, "SensorManager not present")
                showErrorDialog(AppError.SENSOR_MANAGER_NOT_PRESENT)
            }
    }

    private fun registerSensorListener(sensorManager: SensorManager) {
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?.also { rotationVectorSensor ->
                registerRotationVectorSensorListener(
                    sensorManager,
                    rotationVectorSensor
                )
            }
            ?: run {
                Log.w(TAG, "Rotation vector sensor not available")
                showErrorDialog(AppError.ROTATION_VECTOR_SENSOR_NOT_AVAILABLE)
            }

        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            ?.also { magneticFieldSensor ->
                registerMagneticFieldSensorListener(
                    sensorManager,
                    magneticFieldSensor
                )
            }
            ?: run {
                Log.w(TAG, "Magnetic field sensor not available")
                showErrorDialog(AppError.MAGNETIC_FIELD_SENSOR_NOT_AVAILABLE)
            }
    }

    private fun registerRotationVectorSensorListener(
        sensorManager: SensorManager,
        rotationVectorSensor: Sensor
    ) {
        val success = sensorManager.registerListener(
            compassSensorEventListener,
            rotationVectorSensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
        if (success) {
            Log.d(TAG, "Registered listener for rotation vector sensor")
        } else {
            Log.w(TAG, "Could not enable rotation vector sensor")
            showErrorDialog(AppError.ROTATION_VECTOR_SENSOR_FAILED)
        }
    }

    private fun registerMagneticFieldSensorListener(
        sensorManager: SensorManager,
        magneticFieldSensor: Sensor
    ) {
        val success = sensorManager.registerListener(
            compassSensorEventListener,
            magneticFieldSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        if (success) {
            Log.d(TAG, "Registered listener for magnetic field sensor")
        } else {
            Log.w(TAG, "Could not enable magnetic field sensor")
            showErrorDialog(AppError.MAGNETIC_FIELD_SENSOR_FAILED)
        }
    }

    private fun showErrorDialog(appError: AppError) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.error)
            .setIcon(R.drawable.ic_error)
            .setMessage(
                getString(
                    R.string.error_message,
                    getString(appError.messageId),
                    appError.name
                )
            )
            .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(compassSensorEventListener)
        locationRequestCancellationSignal?.cancel()
        Log.i(TAG, "Stopped compass")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sensorManager = null
        locationManager = null
    }

    override fun setupViews() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        setupSystemServices()
        adjustLayoutToSystemBars()
    }

    override fun bindViewModel() {
        //NO TODO here
    }

    private inner class CompassSensorEventListener : SensorEventListener {

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> updateCompass(event)
                Sensor.TYPE_MAGNETIC_FIELD -> Log.v(
                    TAG, "Received magnetic field sensor event ${event.values}"
                )

                else -> Log.w(
                    TAG, "Unexpected sensor changed event of type ${event.sensor.type}"
                )
            }
        }

        private fun updateCompass(event: SensorEvent) {
            val rotationVector =
                RotationVector(event.values[0], event.values[1], event.values[2])
            val displayRotation = getDisplayRotation()
            val magneticAzimuth = MathUtils.calculateAzimuth(rotationVector, displayRotation)
            setAzimuth(magneticAzimuth)
        }

        private fun getDisplayRotation(): DisplayRotation {
            return when (getDisplayCompat()?.rotation) {
                Surface.ROTATION_90 -> DisplayRotation.ROTATION_90
                Surface.ROTATION_180 -> DisplayRotation.ROTATION_180
                Surface.ROTATION_270 -> DisplayRotation.ROTATION_270
                else -> DisplayRotation.ROTATION_0
            }
        }

        private fun getDisplayCompat(): Display? {
            return if (VERSION.SDK_INT >= VERSION_CODES.R) {
                requireContext().display
            } else {
                @Suppress("DEPRECATION")
                requireActivity().windowManager.defaultDisplay
            }
        }
    }

    internal fun setAzimuth(azimuth: Azimuth) {
        Log.v(TAG, "Azimuth $azimuth")
        binding.compass.setAzimuth(azimuth.degrees)
    }
}