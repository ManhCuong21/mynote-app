package com.example.mynote.activity

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

class MotionDetector(
    context: Context,
    private val onMotionDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val lastSensorValues = mutableMapOf<Int, FloatArray?>()

    private val motionThreshold = 30.0

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensorType = event.sensor.type
        val prevValues = lastSensorValues[sensorType]

        prevValues?.let {
            val delta = calculateDelta(event.values, it)
            if (delta > motionThreshold) {
                onMotionDetected()
            }
        }

        lastSensorValues[sensorType] = event.values.clone()
    }

    private fun calculateDelta(current: FloatArray, previous: FloatArray): Double {
        return current.zip(previous)
            .sumOf { (cur, prev) -> abs(cur - prev).toDouble() }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}