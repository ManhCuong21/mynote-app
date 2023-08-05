package com.example.core.core.file.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.fragment.app.FragmentActivity
import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.file.FileExtension
import com.example.core.core.model.ItemImage
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ImageFileImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val fileExtension: FileExtension
) : ImageFile {

    override fun saveImageToFile(
        fragmentActivity: FragmentActivity,
        pathFile: String,
        bitmap: Bitmap
    ) {
        val fileName = "${
            fileExtension.getOutputMediaDirectory(
                fragmentActivity, pathFile
            )
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

    override suspend fun readImageFromFile(
        fragmentActivity: FragmentActivity,
        pathFile: String
    ): List<ItemImage> {
        return withContext(appCoroutineDispatchers.io) {
            val files =
                fileExtension.getOutputMediaDirectory(fragmentActivity, pathFile).listFiles()
            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
                val bytes = it.readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ItemImage(
                    it.absolutePath,
                    image = Bitmap.createBitmap(bmp)
                )
            } ?: listOf()
        }
    }

    override suspend fun deleteImageFromFile(pathImage: String) {
        withContext(appCoroutineDispatchers.io) {
            val file = File(pathImage)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}