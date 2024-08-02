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
            val fileDirectoryTemp = fileRepository.createOrGetDirectory(fragmentActivity, "Temp")
            fileDirectoryTemp.listFiles()
                ?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }
                ?.map {
                    try {
                        val imagePath = "${
                            fileRepository.createOrGetDirectory(fragmentActivity, directoryName)
                        }/${it.name}"
                        val bytes = it.readBytes()
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        val outputStream = FileOutputStream(imagePath)
                        outputStream.flush()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        outputStream.close()
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
            val fileDirectory = fileRepository.createOrGetDirectory(fragmentActivity, directoryName)
            fileDirectory.listFiles()
                ?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }
                ?.map {
                    val fileDirectoryTemp = "${
                        fileRepository.createOrGetDirectory(fragmentActivity, "Temp")
                    }/${it.name}"
                    try {
                        val bytes = it.readBytes()
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        val outputStream = FileOutputStream(fileDirectoryTemp)
                        outputStream.flush()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        outputStream.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }
    }

    override suspend fun saveImageToTemp(fragmentActivity: FragmentActivity, bitmap: Bitmap) {
        withContext(appCoroutineDispatchers.io) {
            val fileName = "${
                fileRepository.createOrGetDirectory(fragmentActivity, "Temp")
            }/${System.currentTimeMillis()}.jpg"
            try {
                val outputStream = FileOutputStream(fileName)
                outputStream.flush()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun readImage(fragmentActivity: FragmentActivity): List<ItemImage> {
        return withContext(appCoroutineDispatchers.io) {
            val listImage = arrayListOf<ItemImage>()
            val fileDirectoryTemp = fileRepository.createOrGetDirectory(fragmentActivity, "Temp")
            fileDirectoryTemp.listFiles()
                ?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }
                ?.map {
                    if (it.exists()) {
                        val bytes = it.readBytes()
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        listImage.add(ItemImage(it.absolutePath, Bitmap.createBitmap(bmp)))
                    }
                }
            listImage
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
}