package com.example.domain.usecase.file

import android.graphics.Bitmap
import androidx.fragment.app.FragmentActivity
import com.example.core.core.model.ItemImage
import com.example.data.file.image.ImageFileRepository
import javax.inject.Inject

class ImageFileUseCase @Inject constructor(
    private val imageFileRepository: ImageFileRepository
) {
    fun saveImageToDirectory(fragmentActivity: FragmentActivity, folderName: String) =
        imageFileRepository.saveImageToDirectory(fragmentActivity, folderName)

    fun saveImageToTemp(fragmentActivity: FragmentActivity, bitmap: Bitmap) =
        imageFileRepository.saveImageToTemp(fragmentActivity, bitmap)

    suspend fun readImage(fragmentActivity: FragmentActivity): List<ItemImage> =
        imageFileRepository.readImage(fragmentActivity)

    suspend fun deleteImage(pathImage: String) = imageFileRepository.deleteImage(pathImage)
}