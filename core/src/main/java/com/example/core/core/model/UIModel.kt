package com.example.core.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryUIModel(
    val id: Int? = null,
    val title: String,
    val image: Int
) : Parcelable

@Parcelize
data class NoteUIModel(
    val id: Int? = null,
    val categoryId: Int,
    val titleNote: String,
    val contentNote: String,
    val fileMediaNote: String,
    var colorNote: Int,
    var timeNote: Long
) : Parcelable