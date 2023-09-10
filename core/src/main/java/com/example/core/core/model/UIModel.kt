package com.example.core.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryModel(
    val idCategory: Int,
    val titleCategory: String,
    val imageCategory: Int
) : Parcelable

@Parcelize
data class NoteModel(
    val idNote: Int,
    val categoryNote: CategoryModel,
    val titleNote: String,
    val contentNote: String,
    val nameMediaNote: String,
    val hasImage: Boolean,
    val hasRecord: Boolean,
    var colorTitleNote: String,
    var colorContentNote: String,
    var timeNote: Long,
    var notificationModel: NotificationModel? = null,
) : Parcelable

@Parcelize
data class NotificationModel(
    val idNotification: Int,
    val dayOfMonth: Long?,
    val dayOfWeek: List<Int>?,
    val hour: Int,
    val minute: Int
) : Parcelable