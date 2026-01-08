package com.example.data.file.file

import android.content.Context
import com.example.core.core.external.AppCoroutineDispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) : FileRepository {
    override fun createOrGetDirectory(directoryPath: String): File {
        val baseDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            ?: context.getExternalFilesDir(null)
            ?: context.filesDir

        val targetFolder = File(baseDir, directoryPath)

        if (!targetFolder.exists()) {
            val result = targetFolder.mkdirs()
            if (!result) {
                return baseDir
            }
        }
        return targetFolder
    }

    override suspend fun saveFileToDirectory(directoryName: String) {
        val tempDir = createOrGetDirectory("Temp")
        val targetDir = createOrGetDirectory(directoryName)

        tempDir.listFiles()
            ?.filter { it.canRead() && it.isFile }
            ?.forEach { file ->
                try {
                    val targetFile = File(targetDir, file.name)

                    file.inputStream().use { input ->
                        targetFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    override suspend fun saveFileToTemp(directoryName: String) {
        withContext(appCoroutineDispatchers.io) {
            val sourceDir = createOrGetDirectory(directoryName)
            val targetDir = createOrGetDirectory("Temp")

            sourceDir.listFiles()
                ?.filter { it.canRead() && it.isFile }
                ?.forEach { file ->
                    try {
                        file.copyTo(File(targetDir, file.name), overwrite = true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }
    }

    override suspend fun deleteDirectory(directoryName: String) {
        withContext(appCoroutineDispatchers.io) {
            val dir = createOrGetDirectory(directoryName)
            dir.deleteRecursively()
        }
    }

    override suspend fun deleteDirectoryTemp() {
        withContext(appCoroutineDispatchers.io) {
            val tempDir = createOrGetDirectory("Temp")
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