package com.example.data.file.image

import android.graphics.Bitmap
import com.example.core.core.model.ItemImage

interface ImageFileRepository {
    suspend fun saveImageToTemp(bitmap: Bitmap)
    suspend fun getListImage(): List<ItemImage>
    suspend fun deleteImage(imagePath: String)
}