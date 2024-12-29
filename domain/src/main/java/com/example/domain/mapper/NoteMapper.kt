package com.example.domain.mapper

import com.example.core.core.model.CategoryModel
import com.example.core.core.model.NoteModel
import com.example.core.core.model.NotificationModel
import com.example.data.datalocal.model.NoteEntity
import com.example.data.datalocal.model.NotificationEntity
import com.example.data.dataremote.model.NoteRemote
import com.example.data.dataremote.model.NotificationRemote

data class NoteParams(
    val categoryNote: CategoryModel,
    val titleNote: String,
    val contentNote: String,
    val fileMediaNote: String,
    val hasImage: Boolean,
    val hasRecord: Boolean,
    var colorTitleNote: String,
    var colorContentNote: String,
    var timeNote: Long,
    val security: Boolean
)

fun NoteParams.toNoteEntity() = NoteEntity(
    categoryEntity = categoryNote.toCategoryEntity(),
    titleNote = titleNote,
    contentNote = contentNote,
    fileMediaNote = fileMediaNote,
    hasImage = hasImage,
    hasRecord = hasRecord,
    colorTitleNote = colorTitleNote,
    colorContentNote = colorContentNote,
    timeNote = timeNote,
    security = security
)

fun NoteParams.toNoteRemote() = NoteRemote(
    // Category
    categoryRemote = categoryNote.toCategoryRemote(),
    // Note
    titleNote = titleNote,
    contentNote = contentNote,
    fileMediaNote = fileMediaNote,
    hasImage = hasImage,
    hasRecord = hasRecord,
    colorTitleNote = colorTitleNote,
    colorContentNote = colorContentNote,
    timeNote = timeNote,
    security = security
)

fun NoteModel.toNoteEntity() = NoteEntity(
    idNote = idNote,
    categoryEntity = categoryNote?.toCategoryEntity(),
    titleNote = titleNote,
    contentNote = contentNote,
    fileMediaNote = nameMediaNote,
    hasImage = hasImage,
    hasRecord = hasRecord,
    colorTitleNote = colorTitleNote,
    colorContentNote = colorContentNote,
    notificationEntity = notificationModel?.toNotificationEntity(),
    timeNote = timeNote,
    security = security
)

fun NoteModel.toNoteRemote() = NoteRemote(
    // Category
    categoryRemote = categoryNote?.toCategoryRemote(),
    // Note
    idNote = idNote,
    titleNote = titleNote,
    contentNote = contentNote,
    fileMediaNote = nameMediaNote,
    hasImage = hasImage,
    hasRecord = hasRecord,
    colorTitleNote = colorTitleNote,
    colorContentNote = colorContentNote,
    timeNote = timeNote,
    // Notification
    notificationRemote = NotificationRemote(
        idNotification = notificationModel?.idNotification,
        dayOfMonth = notificationModel?.dayOfMonth,
        dayOfWeek = notificationModel?.dayOfWeek,
        hour = notificationModel?.hour,
        minute = notificationModel?.minute
    ),
    security = security
)

fun NoteModel.toNoteEntityWithNotification() = NoteEntity(
    idNote = idNote,
    categoryEntity = categoryNote?.toCategoryEntity(),
    titleNote = titleNote,
    contentNote = contentNote,
    fileMediaNote = nameMediaNote,
    hasImage = hasImage,
    hasRecord = hasRecord,
    colorTitleNote = colorTitleNote,
    colorContentNote = colorContentNote,
    timeNote = timeNote,
    notificationEntity = notificationModel?.toNotificationEntity(),
    security = security
)

fun NoteEntity.toNoteModel() = NoteModel(
    idNote = idNote,
    categoryNote = categoryEntity?.toCategory(),
    titleNote = titleNote,
    contentNote = contentNote,
    nameMediaNote = fileMediaNote,
    hasImage = hasImage,
    hasRecord = hasRecord,
    colorTitleNote = colorTitleNote,
    colorContentNote = colorContentNote,
    timeNote = timeNote ?: System.currentTimeMillis(),
    typeNote = typeNote,
    notificationModel = notificationEntity?.toNotification(),
    security = security
)

fun NoteRemote.toNoteModel() = NoteModel(
    idNote = idNote,
    categoryNote = categoryRemote?.toCategoryModel(),
    titleNote = titleNote.orEmpty(),
    contentNote = contentNote.orEmpty(),
    nameMediaNote = fileMediaNote.orEmpty(),
    hasImage = hasImage ?: false,
    hasRecord = hasRecord ?: false,
    colorTitleNote = colorTitleNote.orEmpty(),
    colorContentNote = colorContentNote.orEmpty(),
    timeNote = timeNote ?: System.currentTimeMillis(),
    typeNote = typeNote,
    notificationModel = notificationRemote?.toNotificationModel(),
    security = security
)

private fun NotificationModel.toNotificationEntity() = NotificationEntity(
    idNotification = idNotification,
    dayOfMonth = dayOfMonth,
    dayOfWeek = dayOfWeek,
    hour = hour,
    minute = minute
)

private fun NotificationEntity.toNotification() = NotificationModel(
    idNotification = idNotification,
    dayOfMonth = dayOfMonth,
    dayOfWeek = dayOfWeek,
    hour = hour,
    minute = minute
)

private fun NotificationRemote.toNotificationModel() = NotificationModel(
    idNotification = idNotification,
    dayOfMonth = dayOfMonth,
    dayOfWeek = dayOfWeek,
    hour = hour,
    minute = minute
)