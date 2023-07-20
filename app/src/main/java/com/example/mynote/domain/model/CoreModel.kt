package com.example.mynote.domain.model

import android.graphics.Bitmap

data class ItemImage(val pathImage: String, val image: Bitmap)
data class ItemRecord(val pathRecord: String)
enum class StatusRecord { CREATE, START, PAUSE, RESUME }