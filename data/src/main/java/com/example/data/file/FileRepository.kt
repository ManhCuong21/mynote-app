package com.example.data.file

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.fragment.app.FragmentActivity
import com.example.core.core.external.AppCoroutineDispatchers
import com.github.michaelbull.result.Result
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import com.github.michaelbull.result.runCatching

interface FileRepository {
    fun createDirectory(fragmentActivity: FragmentActivity, pathDirectory: String): File
    fun createDirectoryTemp(fragmentActivity: FragmentActivity): File
    fun saveFileToDirectory(fragmentActivity: FragmentActivity, directoryName: String)
    fun saveFileToTemp(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ): Result<Unit, Throwable>

    suspend fun deleteDirectory(fragmentActivity: FragmentActivity, directoryName: String)
    suspend fun deleteDirectoryTemp(fragmentActivity: FragmentActivity)
}

class FileRepositoryImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) : FileRepository {
    override fun createDirectory(
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

    override fun createDirectoryTemp(fragmentActivity: FragmentActivity): File {
        return createDirectory(fragmentActivity, "Temp")
    }

    override fun saveFileToDirectory(fragmentActivity: FragmentActivity, directoryName: String) {
        val fileDirectoryTemp = createDirectoryTemp(fragmentActivity)
        fileDirectoryTemp.listFiles()
            ?.filter { it.canRead() && it.isFile }
            ?.map {
                try {
                    val pathFile = "${
                        createDirectory(fragmentActivity, directoryName)
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
        val fileDirectory = createDirectory(fragmentActivity, directoryName)
        fileDirectory.listFiles()
            ?.filter { it.canRead() && it.isFile }
            ?.map {
                try {
                    val pathFile = "${createDirectoryTemp(fragmentActivity)}/${it.name}"
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
            val file = createDirectory(fragmentActivity, directoryName)
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
            val file = createDirectoryTemp(fragmentActivity)
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