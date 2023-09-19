package com.example.data.dataremote.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class NotificationRemote(
    val idNotification: Long? = null,
    val dayOfMonth: Long? = null,
    val dayOfWeek: List<Int>? = null,
    val hour: Int? = null,
    val minute: Int? = null
)