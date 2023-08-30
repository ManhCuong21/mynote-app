package com.example.core.core.external

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun formatDate(dateFormat: String, milliSeconds: Long): String? {
    val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
    val calendar: Calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    return formatter.format(calendar.time)
}