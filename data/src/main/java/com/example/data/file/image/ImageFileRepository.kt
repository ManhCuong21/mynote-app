package com.example.data.file.image

import android.graphics.Bitmap
import androidx.fragment.app.FragmentActivity
import com.example.core.core.model.ItemImage

interface ImageFileRepository {
    suspend fun saveImageToDirectory(fragmentActivity: FragmentActivity, directoryName: String)
    fun saveImageToTemp(fragmentActivity: FragmentActivity, bitmap: Bitmap)
    suspend fun readImage(fragmentActivity: FragmentActivity): List<ItemImage>
    suspend fun deleteImage(pathImage: String)
}