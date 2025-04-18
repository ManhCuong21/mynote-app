package com.example.data.file.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.fragment.app.FragmentActivity
import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.model.ItemImage
import com.example.data.file.file.FileRepository
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ImageFileRepositoryImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val fileRepository: FileRepository
) : ImageFileRepository {

    override suspend fun saveImageToDirectory(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ) {
        withContext(appCoroutineDispatchers.io) {
            val tempDir = fileRepository.createOrGetDirectory(fragmentActivity, "Temp")
            val targetDir = fileRepository.createOrGetDirectory(fragmentActivity, directoryName)

            tempDir.listFiles()
                ?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }
                ?.forEach { imageFile ->
                    try {
                        val bytes = imageFile.readBytes()
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        val outputFile = File(targetDir, imageFile.name)
                        saveBitmapToFile(bitmap, outputFile)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }
    }

    override suspend fun saveImageFromDirectoryToTemp(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ) {
        withContext(appCoroutineDispatchers.io) {
            val sourceDir = fileRepository.createOrGetDirectory(fragmentActivity, directoryName)
            val tempDir = fileRepository.createOrGetDirectory(fragmentActivity, "Temp")

            sourceDir.listFiles()
                ?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }
                ?.forEach { imageFile ->
                    try {
                        val bytes = imageFile.readBytes()
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        val outputFile = File(tempDir, imageFile.name)
                        saveBitmapToFile(bitmap, outputFile)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }
    }

    override suspend fun saveImageToTemp(fragmentActivity: FragmentActivity, bitmap: Bitmap) {
        withContext(appCoroutineDispatchers.io) {
            val tempDir = fileRepository.createOrGetDirectory(fragmentActivity, "Temp")
            val outputFile = File(tempDir, "${System.currentTimeMillis()}.jpg")
            saveBitmapToFile(bitmap, outputFile)
        }
    }

    override suspend fun readImage(fragmentActivity: FragmentActivity): List<ItemImage> {
        return withContext(appCoroutineDispatchers.io) {
            val tempDir = fileRepository.createOrGetDirectory(fragmentActivity, "Temp")

            tempDir.listFiles()
                ?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }
                ?.mapNotNull { file ->
                    try {
                        val bytes = file.readBytes()
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        ItemImage(file.absolutePath, Bitmap.createBitmap(bitmap))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                } ?: emptyList()
        }
    }

    override suspend fun deleteImage(imagePath: String) {
        withContext(appCoroutineDispatchers.io) {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        try {
            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}