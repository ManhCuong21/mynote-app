package com.example.data.file.file

import android.content.Context
import com.example.core.core.external.AppCoroutineDispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
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

    override suspend fun zipPicturesDirectory(zipFile: File) {
        withContext(appCoroutineDispatchers.io) {
            // Lấy gốc thư mục Pictures của bạn
            val picturesDir = createOrGetDirectory("")

            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                picturesDir.listFiles()?.forEach { subFolder ->
                    // Chỉ nén các folder bắt đầu bằng MediaNote, bỏ qua folder Temp
                    if (subFolder.isDirectory && subFolder.name.startsWith("MediaNote")) {
                        subFolder.walkTopDown().forEach { file ->
                            if (file.isFile) {
                                // Entry name: "MediaNote2026.../image.png"
                                val entryName = "${subFolder.name}/${file.name}"
                                zos.putNextEntry(ZipEntry(entryName))
                                file.inputStream().use { it.copyTo(zos) }
                                zos.closeEntry()
                            }
                        }
                    }
                }
            }
        }
    }

    override suspend fun unzipFromStream(inputStream: InputStream, targetFolder: File) {
        withContext(appCoroutineDispatchers.io) {
            ZipInputStream(inputStream).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val newFile = File(targetFolder, entry.name)
                    if (entry.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        newFile.parentFile?.mkdirs() // Tạo MediaNote2026...
                        newFile.outputStream().use { output ->
                            zis.copyTo(output)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }
    }
}