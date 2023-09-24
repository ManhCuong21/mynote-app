package com.example.core.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryModel(
    val idCategory: Long,
    val titleCategory: String,
    val imageCategory: Int,
    val typeCategory: Int
) : Parcelable

@Parcelize
data class NoteModel(
    val idNote: Long,
    val categoryNote: CategoryModel?,
    val titleNote: String,
    val contentNote: String,
    val nameMediaNote: String,
    val hasImage: Boolean,
    val hasRecord: Boolean,
    val colorTitleNote: String,
    val colorContentNote: String,
    val timeNote: Long,
    val typeNote: Int,
    val notificationModel: NotificationModel? = null
) : Parcelable

@Parcelize
data class NotificationModel(
    val idNotification: Long?,
    val dayOfMonth: Long?,
    val dayOfWeek: List<Int>?,
    val hour: Int?,
    val minute: Int?
) : Parcelable