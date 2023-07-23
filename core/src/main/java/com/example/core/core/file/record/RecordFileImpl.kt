package com.example.core.core.file.record

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.fragment.app.FragmentActivity
import com.example.core.core.external.AppConstants.FILE_NAME_FORMAT
import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.file.FileExtension
import com.example.core.core.model.ItemRecord
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class RecordFileImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val fileExtension: FileExtension
) : RecordFile {
    override fun saveRecordToFile(
        fragmentActivity: FragmentActivity,
        pathChild: Uri,
        bitmap: Bitmap
    ) {
        val pathFile = SimpleDateFormat(
            FILE_NAME_FORMAT,
            Locale.getDefault()
        ).format(System.currentTimeMillis())
        val recordCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
            contentResolver.insert(recordCollection, contentValues)?.also { uri ->
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

    override suspend fun readRecordFromFile(
        fragmentActivity: FragmentActivity,
        pathFile: String
    ): List<ItemRecord> {
        return withContext(appCoroutineDispatchers.io) {
            val files =
                fileExtension.getOutputMediaDirectory(fragmentActivity, pathFile).listFiles()
            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".mp4") }?.map {
                ItemRecord(pathRecord = it.path)
            } ?: listOf()
        }
    }

    override suspend fun deleteRecordFromFile(pathRecord: String) {
        withContext(appCoroutineDispatchers.io) {
            val file = File(pathRecord)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}