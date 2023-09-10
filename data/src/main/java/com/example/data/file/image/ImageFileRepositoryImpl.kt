package com.example.data.file.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.fragment.app.FragmentActivity
import com.example.core.core.external.AppCoroutineDispatchers
import com.example.data.file.FileRepository
import com.example.core.core.model.ItemImage
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ImageFileRepositoryImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val fileRepository: FileRepository
) : ImageFileRepository {

    override fun saveImageToDirectory(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ) {
        val fileDirectoryTemp = fileRepository.getOutputMediaDirectoryTemp(fragmentActivity)
        fileDirectoryTemp.listFiles()
            ?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }
            ?.map {
                try {
                    val pathImage = "${
                        fileRepository.getOutputMediaDirectory(fragmentActivity, directoryName)
                    }/${it.name}"
                    val bytes = it.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val outputStream = FileOutputStream(pathImage)
                    outputStream.flush()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    override fun saveImageToTemp(fragmentActivity: FragmentActivity, bitmap: Bitmap) {
        val fileName = "${
            fileRepository.getOutputMediaDirectoryTemp(fragmentActivity)
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

    override suspend fun readImage(fragmentActivity: FragmentActivity): List<ItemImage> {
        return withContext(appCoroutineDispatchers.io) {
            val listImage = arrayListOf<ItemImage>()
            val fileDirectoryTemp = fileRepository.getOutputMediaDirectoryTemp(fragmentActivity)
            fileDirectoryTemp.listFiles()
                ?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }
                ?.map {
                    val bytes = it.readBytes()
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    listImage.add(ItemImage(it.absolutePath, Bitmap.createBitmap(bmp)))
                }
            listImage
        }
    }

    override suspend fun deleteImage(pathImage: String) {
        withContext(appCoroutineDispatchers.io) {
            val file = File(pathImage)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}