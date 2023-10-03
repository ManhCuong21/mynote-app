package com.example.data.datalocal.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.core.external.AppConstants.TYPE_LOCAL

@Entity
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "idCategory")
    val idCategory: Long = 0,
    @ColumnInfo(name = "titleCategory")
    val titleCategory: String,
    @ColumnInfo(name = "imageCategory")
    val imageCategory: String,
    @ColumnInfo(name = "typeCategory")
    val typeCategory: Int = TYPE_LOCAL
)