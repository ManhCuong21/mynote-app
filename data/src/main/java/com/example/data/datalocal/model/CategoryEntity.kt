package com.example.data.datalocal.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "idCategory")
    var idCategory: Int = 0,
    @ColumnInfo(name = "titleCategory")
    var titleCategory: String,
    @ColumnInfo(name = "imageCategory")
    var imageCategory: Int
)