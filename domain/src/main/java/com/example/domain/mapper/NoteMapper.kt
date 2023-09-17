package com.example.domain.mapper

import com.example.core.core.model.CategoryModel
import com.example.core.core.model.NoteModel
import com.example.core.core.model.NotificationModel
import com.example.data.datalocal.model.NoteEntity
import com.example.data.datalocal.model.NotificationEntity

data class NoteParams(
    val categoryNote: CategoryModel,
    val titleNote: String,
    val contentNote: String,
    val fileMediaNote: String,
    val hasImage: Boolean,
    val hasRecord: Boolean,
    var colorTitleNote: String,
    var colorContentNote: String,
    var timeNote: Long
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
    timeNote = timeNote
)

fun NoteModel.toNoteEntity() = NoteEntity(
    idNote = idNote,
    categoryEntity = categoryNote.toCategoryEntity(),
    titleNote = titleNote,
    contentNote = contentNote,
    fileMediaNote = nameMediaNote,
    hasImage = hasImage,
    hasRecord = hasRecord,
    colorTitleNote = colorTitleNote,
    colorContentNote = colorContentNote,
    notificationEntity = notificationModel?.toNotificationEntity(),
    timeNote = timeNote
)

fun NoteModel.toNoteEntityWithNotification() = NoteEntity(
    idNote = idNote,
    categoryEntity = categoryNote.toCategoryEntity(),
    titleNote = titleNote,
    contentNote = contentNote,
    fileMediaNote = nameMediaNote,
    hasImage = hasImage,
    hasRecord = hasRecord,
    colorTitleNote = colorTitleNote,
    colorContentNote = colorContentNote,
    timeNote = timeNote,
    notificationEntity = notificationModel?.toNotificationEntity()
)

fun NoteEntity.toNoteModel() = NoteModel(
    idNote = idNote,
    categoryNote = categoryEntity.toCategory(),
    titleNote = titleNote,
    contentNote = contentNote,
    nameMediaNote = fileMediaNote,
    hasImage = hasImage,
    hasRecord = hasRecord,
    colorTitleNote = colorTitleNote,
    colorContentNote = colorContentNote,
    timeNote = timeNote ?: System.currentTimeMillis(),
    notificationModel = notificationEntity?.toNotificationModel()
)

private fun NotificationModel.toNotificationEntity() = NotificationEntity(
    idNotification = idNotification,
    dayOfMonth = dayOfMonth,
    dayOfWeek = dayOfWeek,
    hour = hour,
    minute = minute
)

private fun NotificationEntity.toNotificationModel() = NotificationModel(
    idNotification = idNotification,
    dayOfMonth = dayOfMonth,
    dayOfWeek = dayOfWeek,
    hour = hour,
    minute = minute
)