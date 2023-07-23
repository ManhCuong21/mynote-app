package com.example.core.core.model

import android.graphics.Bitmap

data class ItemImage(val pathImage: String, val image: Bitmap)
data class ItemRecord(val pathRecord: String)
data class ListDialogItem(
    val title: String,
    val image: Any,
    val isVisibleCheckBox: Boolean = true
)
enum class StatusRecord { CREATE, START, PAUSE, RESUME }