package com.example.data.datalocal.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.core.external.AppConstants.TYPE_LOCAL

@Entity
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "idNote")
    val idNote: Long = 0,
    @Embedded
    val categoryEntity: CategoryEntity?,
    @ColumnInfo(name = "titleNote")
    val titleNote: String,
    @ColumnInfo(name = "contentNote")
    val contentNote: String,
    @ColumnInfo(name = "fileMediaNote")
    val fileMediaNote: String,
    @ColumnInfo(name = "hasImage")
    val hasImage: Boolean,
    @ColumnInfo(name = "hasRecord")
    val hasRecord: Boolean,
    @ColumnInfo(name = "colorTitleNote")
    val colorTitleNote: String,
    @ColumnInfo(name = "colorContentNote")
    val colorContentNote: String,
    @ColumnInfo(name = "timeNote")
    val timeNote: Long?,
    @ColumnInfo(name = "typeNote")
    val typeNote: Int = TYPE_LOCAL,
    @Embedded
    val notificationEntity: NotificationEntity? = null
)