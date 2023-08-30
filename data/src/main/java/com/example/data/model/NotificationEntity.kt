package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("idNotification")
    val idNotification: Int,
    @SerializedName("dayOfMonth")
    val dayOfMonth: Long?,
    @SerializedName("dayOfWeek")
    val dayOfWeek: List<Int>?,
    @SerializedName("hour")
    val hour: Int,
    @SerializedName("minute")
    val minute: Int
)
