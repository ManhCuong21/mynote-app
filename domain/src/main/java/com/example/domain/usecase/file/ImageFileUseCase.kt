package com.example.domain.usecase.file

import android.graphics.Bitmap
import androidx.fragment.app.FragmentActivity
import com.example.core.core.model.ItemImage
import com.example.data.file.image.ImageFileRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import javax.inject.Inject

class ImageFileUseCase @Inject constructor(
    private val imageFileRepository: ImageFileRepository
) {
    suspend fun saveImageToDirectory(
        fragmentActivity: FragmentActivity,
        folderName: String
    ): Result<Unit, Throwable> =
        runCatching {
            imageFileRepository.saveImageToDirectory(fragmentActivity, folderName)
        }

    suspend fun saveImageToTemp(fragmentActivity: FragmentActivity, bitmap: Bitmap) =
        imageFileRepository.saveImageToTemp(fragmentActivity, bitmap)

    suspend fun saveImageFromDirectoryToTemp(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ) = imageFileRepository.saveImageFromDirectoryToTemp(fragmentActivity, directoryName)

    suspend fun readImage(fragmentActivity: FragmentActivity): List<ItemImage> =
        imageFileRepository.readImage(fragmentActivity)

    suspend fun deleteImage(imagePath: String) = imageFileRepository.deleteImage(imagePath)
}