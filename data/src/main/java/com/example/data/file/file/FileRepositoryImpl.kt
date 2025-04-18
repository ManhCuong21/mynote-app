package com.example.data.file.file

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.fragment.app.FragmentActivity
import com.example.core.core.external.AppCoroutineDispatchers
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) : FileRepository {
    override fun createOrGetDirectory(
        fragmentActivity: FragmentActivity,
        directoryPath: String
    ): File {
        val mediaDir = fragmentActivity.externalMediaDirs.firstOrNull()?.let { file ->
            File(file, directoryPath).apply {
                mkdirs()
            }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else fragmentActivity.filesDir
    }

    override fun saveFileToDirectory(fragmentActivity: FragmentActivity, directoryName: String) {
        val fileDirectoryTemp = createOrGetDirectory(fragmentActivity, "Temp")
        fileDirectoryTemp.listFiles()
            ?.filter { it.canRead() && it.isFile }
            ?.map {
                try {
                    val pathFile = "${
                        createOrGetDirectory(fragmentActivity, directoryName)
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

    override fun saveFileToTemp(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ): Result<Unit, Throwable> = runCatching {
        val fileDirectory = createOrGetDirectory(fragmentActivity, directoryName)
        fileDirectory.listFiles()
            ?.filter { it.canRead() && it.isFile }
            ?.map {
                try {
                    val pathFile = "${createOrGetDirectory(fragmentActivity, "Temp")}/${it.name}"
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

    override suspend fun deleteDirectory(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ) {
        withContext(appCoroutineDispatchers.io) {
            val dirToDelete = createOrGetDirectory(fragmentActivity, directoryName)
            if (dirToDelete.exists() && dirToDelete.isDirectory) {
                dirToDelete.listFiles()?.forEach { file ->
                    if (file.exists()) {
                        file.delete()
                    }
                }
                dirToDelete.delete()
            }
        }
    }

    override suspend fun deleteDirectoryTemp(fragmentActivity: FragmentActivity) {
        withContext(appCoroutineDispatchers.io) {
            val tempDir = createOrGetDirectory(fragmentActivity, "Temp")
            if (tempDir.exists() && tempDir.isDirectory) {
                tempDir.listFiles()?.forEach { child ->
                    if (child.isDirectory) {
                        child.deleteRecursively()
                    } else {
                        child.delete()
                    }
                }
                tempDir.delete()
            }
        }
    }
}