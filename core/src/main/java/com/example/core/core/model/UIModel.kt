package com.example.core.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryUIModel(
    var id: Int? = null,
    var title: String,
    var image: Int
) : Parcelable

@Parcelize
data class NoteUIModel(
    val id: Int = 0
) : Parcelable