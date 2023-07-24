package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,
    @ColumnInfo(name = "categoryId")
    var categoryId: Int?,
    @ColumnInfo(name = "title")
    var titleNote: String?,
    @ColumnInfo(name = "text")
    var contentNote: String?,
    @ColumnInfo(name = "image")
    var fileMediaNote: String,
    @ColumnInfo(name = "color")
    var colorNote: Int?,
    @ColumnInfo(name = "time")
    var timeNote: Long?
)
