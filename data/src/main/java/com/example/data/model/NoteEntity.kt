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
    var title: String?,
    @ColumnInfo(name = "text")
    var text: String?,
    @ColumnInfo(name = "image")
    var image: List<String>?,
    @ColumnInfo(name = "record")
    var record: List<String>?,
    @ColumnInfo(name = "color")
    var color: String?,
    @ColumnInfo(name = "time")
    var time: Long?
)
