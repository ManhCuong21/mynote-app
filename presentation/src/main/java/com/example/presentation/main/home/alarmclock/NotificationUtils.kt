package com.example.presentation.main.home.alarmclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import com.example.core.core.external.AppConstants.KEY_ALARM_SOUND_NOTIFICATION
import com.example.core.core.external.AppConstants.KEY_CHANNEL_ID_NOTIFICATION
import com.example.core.core.external.AppConstants.KEY_CONTENT_NOTIFICATION
import com.example.core.core.external.AppConstants.KEY_TITLE_NOTIFICATION
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object NotificationUtils {
    fun setNotificationDayOfMonth(
        context: Context,
        title: String,
        content: String,
        alarmSound: String,
        time: Long,
        hour: Int,
        minute: Int,
        requestCode: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val pendingIntent = Intent(context, NotificationReceiver::class.java).let { intent ->
                intent.putExtra(KEY_CHANNEL_ID_NOTIFICATION, "my_channel_id")
                intent.putExtra(KEY_TITLE_NOTIFICATION, title)
                intent.putExtra(KEY_CONTENT_NOTIFICATION, content)
                intent.putExtra(KEY_ALARM_SOUND_NOTIFICATION, alarmSound)
                PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            val calendar: Calendar = Calendar.getInstance().apply {
                timeInMillis = time
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }
            safeSetExactAlarm(
                context,
                calendar,
                pendingIntent
            )
            Timber.tag("ALARM").d(
                "Đặt alarm vào: ${
                    SimpleDateFormat(
                        "HH:mm:ss dd/MM/yyyy",
                        Locale.getDefault()
                    ).format(calendar.time)
                }"
            )
        }
    }

    fun setNotificationDayOfWeek(
        context: Context,
        title: String,
        content: String,
        alarmSound: String,
        dayOfWeek: List<Int>,
        hour: Int,
        minute: Int,
        requestCodeBase: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()

        dayOfWeek.forEach { day ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                while (get(Calendar.DAY_OF_WEEK) != day) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val requestCode = requestCodeBase * 10 + day

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra(KEY_CHANNEL_ID_NOTIFICATION, "weekly_channel_id")
                putExtra(KEY_TITLE_NOTIFICATION, title)
                putExtra(KEY_CONTENT_NOTIFICATION, content)
                putExtra(KEY_ALARM_SOUND_NOTIFICATION, alarmSound)
                putExtra("REPEAT_WEEKLY", true) // dấu hiệu để đặt lại
                putExtra("WEEKLY_HOUR", hour)
                putExtra("WEEKLY_MINUTE", minute)
                putExtra("WEEKLY_DAY", day)
                putExtra("REQUEST_CODE", requestCodeBase)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )


            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun safeSetExactAlarm(
        context: Context,
        calendar: Calendar,
        pendingIntent: PendingIntent
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${context.packageName}".toUri()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            }
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            Timber.tag("ALARM").e("❌ Lỗi khi đặt alarm: ${e.message}")
        }
    }

    fun cancelAlarm(
        context: Context,
        requestCode: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = Intent(context, NotificationReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }
    }
}