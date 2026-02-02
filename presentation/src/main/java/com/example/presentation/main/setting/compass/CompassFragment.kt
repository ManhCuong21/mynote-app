package com.example.presentation.main.setting.compass

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.Display
import android.view.Surface
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
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
import kotlin.math.abs

const val OPTION_INSTRUMENTED_TEST = "INSTRUMENTED_TEST"

class CompassFragment : BaseFragment(R.layout.fragment_compass) {
    private val compassSensorEventListener = CompassSensorEventListener()
    override val binding: FragmentCompassBinding by viewBinding()
    override val viewModel: BaseViewModel by viewModels()
    private var sensorManager: SensorManager? = null
    private var locationManager: LocationManager? = null
    private var mediaPlayer: MediaPlayer? = null
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
        initMediaPlayer()
        if (!isInstrumentedTest()) {
            registerSensorListener()
        }
    }

    private fun isInstrumentedTest() =
        requireActivity().intent.extras?.getBoolean(OPTION_INSTRUMENTED_TEST) == true

    private fun initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alert_sound)
        }
    }

    private fun registerSensorListener() {
        val sm = sensorManager ?: return
        val sensors = listOf(
            Sensor.TYPE_ROTATION_VECTOR to SensorManager.SENSOR_DELAY_FASTEST,
            Sensor.TYPE_MAGNETIC_FIELD to SensorManager.SENSOR_DELAY_NORMAL,
            Sensor.TYPE_ACCELEROMETER to SensorManager.SENSOR_DELAY_NORMAL,
            Sensor.TYPE_GYROSCOPE to SensorManager.SENSOR_DELAY_NORMAL
        )

        sensors.forEach { (type, delay) ->
            sm.getDefaultSensor(type)?.let { sensor ->
                sm.registerListener(compassSensorEventListener, sensor, delay)
            } ?: run {
                if (type == Sensor.TYPE_ROTATION_VECTOR) {
                    showErrorDialog()
                }
            }
        }
    }

    private fun showErrorDialog() {
        val appError = AppError.ROTATION_VECTOR_SENSOR_NOT_AVAILABLE
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
            .setPositiveButton(R.string.title_ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(compassSensorEventListener)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
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
        private val lastSensorValues = mutableMapOf<Int, FloatArray>()
        private val motionThreshold = 30.0

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> updateCompass(event)
                Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE -> detectMotion(event)
            }
        }

        private fun detectMotion(event: SensorEvent) {
            val sensorType = event.sensor.type
            val currentValues = event.values
            val prevValues = lastSensorValues[sensorType]

            if (prevValues != null) {
                var delta = 0.0
                for (i in currentValues.indices) {
                    delta += abs(currentValues[i] - prevValues[i])
                }

                if (delta > motionThreshold) {
                    playAlertSound()
                }
            }

            lastSensorValues[sensorType] = currentValues.clone()
        }

        private fun playAlertSound() {
            mediaPlayer?.let { mp ->
                if (!mp.isPlaying) {
                    mp.start()
                }
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
        binding.compass.setAzimuth(azimuth.degrees)
    }
}