package com.example.core.core.file.image

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.fragment.app.FragmentActivity
import com.example.core.core.external.AppConstants.FILE_NAME_FORMAT
import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.file.FileExtension
import com.example.core.core.model.ItemImage
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class ImageFileImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val fileExtension: FileExtension
) : ImageFile {

    override fun saveImageToFile(
        fragmentActivity: FragmentActivity,
        pathChild: Uri,
        bitmap: Bitmap
    ) {
        val pathFile = SimpleDateFormat(
            FILE_NAME_FORMAT,
            Locale.getDefault()
        ).format(System.currentTimeMillis())
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_INTERNAL)
        } else MediaStore.Images.Media.INTERNAL_CONTENT_URI
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$pathFile.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
        }
        val contentResolver = fragmentActivity.contentResolver
        try {
            contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                contentResolver.openOutputStream(uri).use { outputStream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                        throw IOException("Couldn't save bitmap")
                    }
                }
            }
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