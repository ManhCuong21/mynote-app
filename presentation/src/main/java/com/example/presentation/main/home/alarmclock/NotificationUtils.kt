package com.example.presentation.main.home.alarmclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.core.core.external.AppConstants.KEY_CHANNEL_ID_NOTIFICATION
import com.example.core.core.external.AppConstants.KEY_CONTENT_NOTIFICATION
import com.example.core.core.external.AppConstants.KEY_TITLE_NOTIFICATION
import java.util.Calendar

object NotificationUtils {
    fun setNotificationDayOfMonth(
        context: Context,
        title: String,
        content: String,
        time: Long,
        hour: Int,
        minute: Int,
        requestCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = Intent(context, NotificationReceiver::class.java).let { intent ->
            intent.putExtra(KEY_CHANNEL_ID_NOTIFICATION, requestCode.toString())
            intent.putExtra(KEY_TITLE_NOTIFICATION, title)
            intent.putExtra(KEY_CONTENT_NOTIFICATION, content)
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
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun setNotificationDayOfWeek(
        context: Context,
        title: String,
        content: String,
        dayOfWeek: List<Int>,
        hour: Int,
        minute: Int,
        requestCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = Intent(context, NotificationReceiver::class.java).let { intent ->
            intent.putExtra(KEY_CHANNEL_ID_NOTIFICATION, requestCode.toString())
            intent.putExtra(KEY_TITLE_NOTIFICATION, title)
            intent.putExtra(KEY_CONTENT_NOTIFICATION, content)
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        dayOfWeek.forEach {
            calendar.set(Calendar.DAY_OF_WEEK, it)
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(
        context: Context,
        requestCode: Int
    ) {
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