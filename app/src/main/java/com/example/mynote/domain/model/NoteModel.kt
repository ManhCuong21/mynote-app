package com.example.mynote.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoteModel(
    val id: Int = 0
) : Parcelable
