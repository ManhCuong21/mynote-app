package com.example.core.core.model

import android.graphics.Bitmap

data class ItemCategory(val title: String, val image: String)
data class ItemChooseColor(val colorTitle: Int, val colorContent: Int)
data class ItemImage(val imagePath: String, val image: Bitmap)
data class ItemRecord(
    val directoryPath: String,
    val recordPath: String,
    val amplitudes: List<Float>
)

data class ListDialogItem(
    val title: String, val image: String, val isVisibleCheckBox: Boolean = true
)

enum class StatusRecord { CREATE, START, PAUSE, RESUME }