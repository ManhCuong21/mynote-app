package com.example.mynote.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryModel(
    var id: Int? = null,
    var title: String,
    var image: Int
) : Parcelable