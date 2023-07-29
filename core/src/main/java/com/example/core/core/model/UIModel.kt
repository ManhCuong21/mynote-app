package com.example.core.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryUIModel(
    val idCategory: Int,
    val titleCategory: String,
    val imageCategory: Int
) : Parcelable

@Parcelize
data class NoteUIModel(
    val idNote: Int,
    val categoryNote: CategoryUIModel,
    val titleNote: String,
    val contentNote: String,
    val fileMediaNote: String,
    var colorTitleNote: String,
    var colorContentNote: String,
    var timeNote: Long
) : Parcelable