package com.example.mynote.core.external

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.mynote.core.external.AppConstants.Companion.FILE_NAME_FORMAT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


interface FileExtension {
    fun getOutputDirectory(activity: Activity, pathDirectory: String): File
    fun saveImageToFile(activity: Activity, pathChild: String, bitmap: Bitmap)
    suspend fun readImageFromFile(
        activity: Activity, pathChild: String, pathListImage: String
    ): List<Bitmap>
}

class FileExtensionImpl @Inject constructor() : FileExtension {

    override fun getOutputDirectory(activity: Activity, pathDirectory: String): File {
        val mediaDir = activity.externalMediaDirs.firstOrNull()?.let { file ->
            File(file, pathDirectory).apply {
                mkdir()
            }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else activity.filesDir
    }

    override fun saveImageToFile(activity: Activity, pathChild: String, bitmap: Bitmap) {
        val directory = getOutputDirectory(activity, pathChild)
        val pathFile = SimpleDateFormat(
            FILE_NAME_FORMAT,
            Locale.getDefault()
        ).format(System.currentTimeMillis())
        val file = File(directory, "$pathFile.jpg")

        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun readImageFromFile(
        activity: Activity,
        pathChild: String,
        pathListImage: String
    ): List<Bitmap> {
        return withContext(Dispatchers.IO) {
            val files = File(getOutputDirectory(activity, pathChild), pathListImage).listFiles()
            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
                val bytes = it.readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                Bitmap.createBitmap(bmp)
            } ?: listOf()
        }
    }
}

