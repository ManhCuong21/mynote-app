package com.example.data.dataremote.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class NotificationRemote(
    val idNotification: Int,
    val dayOfMonth: Long?,
    val dayOfWeek: List<Int>?,
    val hour: Int,
    val minute: Int
)