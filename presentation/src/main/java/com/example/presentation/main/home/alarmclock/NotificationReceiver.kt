package com.example.presentation.main.home.alarmclock

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.core.core.external.AppConstants.KEY_CHANNEL_ID_NOTIFICATION
import com.example.core.core.external.AppConstants.KEY_CONTENT_NOTIFICATION
import com.example.core.core.external.AppConstants.KEY_TITLE_NOTIFICATION
import com.example.presentation.R
import com.example.presentation.main.MainFragment

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val channelId = intent.getStringExtra(KEY_CHANNEL_ID_NOTIFICATION)
        val titleNotification = intent.getStringExtra(KEY_TITLE_NOTIFICATION)
        val contentNotification = intent.getStringExtra(KEY_CONTENT_NOTIFICATION)
        val intentAlarm = Intent(context, MainFragment::class.java)
        intentAlarm.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            context, channelId?.toInt() ?: 0, intentAlarm,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, channelId.toString())
            .setSmallIcon(R.drawable.icon_notification)
            .setContentTitle(titleNotification)
            .setContentText(contentNotification)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        val important = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, titleNotification, important)
        channel.description = contentNotification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.createNotificationChannel(channel)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        if (channelId != null) {
            notificationManager.notify(channelId.toInt(), builder.build())
            NotificationUtils.cancelAlarm(context, channelId.toInt())
        }
    }
}