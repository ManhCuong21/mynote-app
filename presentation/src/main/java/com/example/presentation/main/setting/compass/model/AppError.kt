
package com.example.presentation.main.setting.compass.model

import androidx.annotation.StringRes
import com.example.presentation.R

enum class AppError(@param:StringRes val messageId: Int) {
    SENSOR_MANAGER_NOT_PRESENT(R.string.sensor_error_message),
    ROTATION_VECTOR_SENSOR_NOT_AVAILABLE(R.string.sensor_error_message),
    ROTATION_VECTOR_SENSOR_FAILED(R.string.sensor_error_message),
    MAGNETIC_FIELD_SENSOR_NOT_AVAILABLE(R.string.sensor_error_message),
    MAGNETIC_FIELD_SENSOR_FAILED(R.string.sensor_error_message)
}
