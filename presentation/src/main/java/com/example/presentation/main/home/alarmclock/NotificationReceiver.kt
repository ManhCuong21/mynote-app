package com.example.presentation.main.home.alarmclock

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.example.core.core.external.AppConstants.KEY_ALARM_SOUND_NOTIFICATION
import com.example.core.core.external.AppConstants.KEY_CHANNEL_ID_NOTIFICATION
import com.example.core.core.external.AppConstants.KEY_CONTENT_NOTIFICATION
import com.example.core.core.external.AppConstants.KEY_TITLE_NOTIFICATION
import com.example.presentation.R
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val channelIdRaw = intent.getStringExtra(KEY_CHANNEL_ID_NOTIFICATION)
        val titleNotification = intent.getStringExtra(KEY_TITLE_NOTIFICATION)
        val contentNotification = intent.getStringExtra(KEY_CONTENT_NOTIFICATION)
        val soundResName = intent.getStringExtra(KEY_ALARM_SOUND_NOTIFICATION) ?: "morning_clock"

        val launchIntent = Intent().apply {
            setClassName(context.packageName, "com.example.mynote.activity.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val requestCode = channelIdRaw?.toIntOrNull() ?: 0
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 🔊 Tạo kênh riêng với chuông tùy chọn
        val channelId = createAlarmChannel(context, soundResName)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.icon_notification)
            .setContentTitle(titleNotification)
            .setContentText(contentNotification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        }

        if (channelIdRaw != null) {
            NotificationUtils.cancelAlarm(context, requestCode)
        }

        // 🔁 Lặp lại hàng tuần (nếu có)
        if (intent.getBooleanExtra("REPEAT_WEEKLY", false)) {
            val hour = intent.getIntExtra("WEEKLY_HOUR", 8)
            val minute = intent.getIntExtra("WEEKLY_MINUTE", 0)
            val day = intent.getIntExtra("WEEKLY_DAY", Calendar.MONDAY)
            val requestCodeBase = intent.getIntExtra("REQUEST_CODE", 100)

            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 7)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val newIntent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra(
                    KEY_CHANNEL_ID_NOTIFICATION,
                    "weekly_channel_${requestCodeBase * 10 + day}"
                )
                putExtra(KEY_TITLE_NOTIFICATION, titleNotification)
                putExtra(KEY_CONTENT_NOTIFICATION, contentNotification)
                putExtra("REPEAT_WEEKLY", true)
                putExtra("WEEKLY_HOUR", hour)
                putExtra("WEEKLY_MINUTE", minute)
                putExtra("WEEKLY_DAY", day)
                putExtra("REQUEST_CODE", requestCodeBase)
                putExtra("SOUND_RES_NAME", soundResName)
            }

            val newPendingIntent = PendingIntent.getBroadcast(
                context,
                requestCodeBase * 10 + day,
                newIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                newPendingIntent
            )
        }
    }

    private fun createAlarmChannel(context: Context, resName: String): String {
        val channelId = "reminder_$resName"
        val notifMgr = context.getSystemService(NotificationManager::class.java)

        if (notifMgr.getNotificationChannel(channelId) == null) {
            val soundUri = "android.resource://${context.packageName}/raw/$resName".toUri()
            val audioAttr = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            val channel = NotificationChannel(
                channelId,
                "Reminder • $resName",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(soundUri, audioAttr)
                enableVibration(true)
                enableLights(true)
                description = "Notification sound: $resName"
            }
            notifMgr.createNotificationChannel(channel)
        }
        return channelId
    }
}