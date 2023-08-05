package com.example.core.core.file

import androidx.fragment.app.FragmentActivity
import com.example.core.core.external.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

interface FileExtension {
    fun getOutputMediaDirectory(fragmentActivity: FragmentActivity, pathDirectory: String): File
    suspend fun deleteFile(file: File)
}

class FileExtensionImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) : FileExtension {
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
}