package com.example.data.file

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.fragment.app.FragmentActivity
import com.example.core.core.external.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

interface FileRepository {
    fun getOutputMediaDirectory(fragmentActivity: FragmentActivity, pathDirectory: String): File
    fun getOutputMediaDirectoryTemp(fragmentActivity: FragmentActivity): File
    fun saveFileToDirectory(fragmentActivity: FragmentActivity, directoryName: String)
    fun saveFileToTemp(fragmentActivity: FragmentActivity, directoryName: String)
    suspend fun deleteFile(file: File)
    suspend fun deleteDirectory(file: File)
    suspend fun deleteDirectoryTemp(fragmentActivity: FragmentActivity)
}

class FileRepositoryImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) : FileRepository {
    override fun getOutputMediaDirectory(
        fragmentActivity: FragmentActivity,
        pathDirectory: String
    ): File {
        val mediaDir = fragmentActivity.externalMediaDirs.firstOrNull()?.let { file ->
            File(file, pathDirectory).apply {
                mkdir()
            }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else fragmentActivity.filesDir
    }

    override fun getOutputMediaDirectoryTemp(fragmentActivity: FragmentActivity): File {
        val mediaDir = fragmentActivity.externalMediaDirs.firstOrNull()?.let { file ->
            File(file, "Temp").apply {
                mkdir()
            }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else fragmentActivity.filesDir
    }

    override fun saveFileToDirectory(fragmentActivity: FragmentActivity, directoryName: String) {
        val fileDirectoryTemp = getOutputMediaDirectoryTemp(fragmentActivity)
        fileDirectoryTemp.listFiles()
            ?.filter { it.canRead() && it.isFile }
            ?.map {
                try {
                    val pathFile = "${
                        getOutputMediaDirectory(fragmentActivity, directoryName)
                    }/${it.name}"
                    val bytes = it.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val outputStream = FileOutputStream(pathFile)
                    outputStream.flush()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    override fun saveFileToTemp(fragmentActivity: FragmentActivity, directoryName: String) {
        val fileDirectory = getOutputMediaDirectory(fragmentActivity, directoryName)
        fileDirectory.listFiles()
            ?.filter { it.canRead() && it.isFile }
            ?.map {
                try {
                    val pathFile = "${getOutputMediaDirectoryTemp(fragmentActivity)}/${it.name}"
                    val bytes = it.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val outputStream = FileOutputStream(pathFile)
                    outputStream.flush()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    override suspend fun deleteFile(file: File) {
        withContext(appCoroutineDispatchers.io) {
            if (file.exists()) {
                file.delete()
            }
            if (file.isDirectory) {
                file.listFiles()?.forEach {
                    deleteFile(it)
                }
            }
        }
    }

    override suspend fun deleteDirectory(file: File) {
        withContext(appCoroutineDispatchers.io) {
            if (file.isDirectory) {
                file.listFiles()?.forEach {
                    if (it.exists()) {
                        it.delete()
                    }
                }
            }
        }
    }

    override suspend fun deleteDirectoryTemp(fragmentActivity: FragmentActivity) {
        withContext(appCoroutineDispatchers.io) {
            val file = getOutputMediaDirectoryTemp(fragmentActivity)
            if (file.isDirectory) {
                file.listFiles()?.forEach {
                    if (it.exists()) {
                        it.delete()
                    }
                }
            }
        }
    }
}