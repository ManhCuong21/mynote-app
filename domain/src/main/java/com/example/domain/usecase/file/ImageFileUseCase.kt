package com.example.domain.usecase.file

import android.graphics.Bitmap
import com.example.core.core.model.ItemImage
import com.example.data.file.image.ImageFileRepository
import javax.inject.Inject

class ImageFileUseCase @Inject constructor(
    private val imageFileRepository: ImageFileRepository
) {
    suspend fun saveImageToTemp(bitmap: Bitmap) =
        imageFileRepository.saveImageToTemp(bitmap)

    suspend fun getListImage(): List<ItemImage> =
        imageFileRepository.getListImage()

    suspend fun deleteImage(imagePath: String) = imageFileRepository.deleteImage(imagePath)
}