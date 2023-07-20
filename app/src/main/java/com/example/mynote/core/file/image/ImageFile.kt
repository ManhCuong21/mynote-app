package com.example.mynote.core.file.image

import android.graphics.Bitmap
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.example.mynote.domain.model.ItemImage

interface ImageFile {
    fun saveImageToFile(fragmentActivity: FragmentActivity, pathChild: Uri, bitmap: Bitmap)
    suspend fun readImageFromFile(
        fragmentActivity: FragmentActivity,
        pathFile: String
    ): List<ItemImage>

    suspend fun deleteImageFromFile(
        pathImage: String
    )
}