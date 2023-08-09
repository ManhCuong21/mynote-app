package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "idNote")
    val idNote: Int = 0,
    @Embedded
    val categoryEntity: CategoryEntity,
    @ColumnInfo(name = "titleNote")
    val titleNote: String,
    @ColumnInfo(name = "contentNote")
    val contentNote: String,
    @ColumnInfo(name = "fileMediaNote")
    val fileMediaNote: String,
    @ColumnInfo(name = "colorTitleNote")
    val colorTitleNote: String,
    @ColumnInfo(name = "colorContentNote")
    val colorContentNote: String,
    @ColumnInfo(name = "timeNote")
    val timeNote: Long?
)