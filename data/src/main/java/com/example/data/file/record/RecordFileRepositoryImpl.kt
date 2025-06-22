package com.example.data.file.record

import androidx.fragment.app.FragmentActivity
import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.model.ItemRecord
import com.example.data.file.file.FileRepository
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import javax.inject.Inject

class RecordFileRepositoryImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val fileRepository: FileRepository
) : RecordFileRepository {
    override suspend fun saveAmplitude(
        file: File,
        amplitudes: List<Float>
    ) {
        withContext(appCoroutineDispatchers.io) {
            try {
                if (!file.exists()) {
                    file.mkdirs()
                }

                val amplitudeFile = File(file, "amplitudes.txt")
                ObjectOutputStream(FileOutputStream(amplitudeFile)).use { output ->
                    output.writeObject(amplitudes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun saveRecordToDirectory(
        fragmentActivity: FragmentActivity,
        directoryPath: String
    ) {
        withContext(appCoroutineDispatchers.io) {
            val tempDir = fileRepository.createOrGetDirectory(fragmentActivity, "Temp")
            val recordDirs = tempDir.listFiles()
                ?.filter { it.canRead() && it.isDirectory && it.name.startsWith("Record") }
                ?: return@withContext

            for (recordDir in recordDirs) {
                val files = recordDir.listFiles() ?: continue
                val targetDir = fileRepository.createOrGetDirectory(
                    fragmentActivity,
                    "$directoryPath/${recordDir.name}"
                )

                for (file in files) {
                    val targetFile = File(targetDir, file.name)
                    try {
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
        }
    }

    override suspend fun saveRecordFromDirectoryToTemp(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ) {
        withContext(appCoroutineDispatchers.io) {
            val sourceDir = fileRepository.createOrGetDirectory(fragmentActivity, directoryName)
            val recordDirs = sourceDir.listFiles()
                ?.filter { it.canRead() && it.isDirectory && it.name.startsWith("Record") }
                ?: return@withContext

            for (recordDir in recordDirs) {
                val files = recordDir.listFiles() ?: continue
                val targetDir = fileRepository.createOrGetDirectory(
                    fragmentActivity,
                    "Temp/${recordDir.name}"
                )

                for (file in files) {
                    val targetFile = File(targetDir, file.name)
                    try {
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
        }
    }

    override suspend fun readRecord(fragmentActivity: FragmentActivity): List<ItemRecord> {
        return withContext(appCoroutineDispatchers.io) {
            val tempDir = fileRepository.createOrGetDirectory(fragmentActivity, "Temp")
            val recordDirs = tempDir.listFiles()
                ?.filter { it.canRead() && it.isDirectory && it.name.startsWith("Record") }
                ?: return@withContext emptyList()

            recordDirs.mapNotNull { recordDir ->
                val files = recordDir.listFiles()?.filter { it.canRead() && it.isFile }
                    ?: return@mapNotNull null

                val amplitudePath = files
                    .firstOrNull { it.name.startsWith("amplitude") }
                    ?.path ?: return@mapNotNull null

                val videoFile = files
                    .firstOrNull { it.name.endsWith(".mp4") }
                    ?: return@mapNotNull null

                ItemRecord(
                    directoryPath = recordDir.path,
                    recordPath = videoFile.path,
                    amplitudes = convertFileToFloatArray(amplitudePath)
                )
            }
        }
    }

    private suspend fun convertFileToFloatArray(filePath: String): List<Float> {
        return withContext(appCoroutineDispatchers.io) {
            try {
                FileInputStream(filePath).use { fileInputStream ->
                    ObjectInputStream(fileInputStream).use { objectInputStream ->
                        @Suppress("UNCHECKED_CAST")
                        return@withContext objectInputStream.readObject() as? ArrayList<Float>
                            ?: emptyList()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
            emptyList()
        }
    }

    override suspend fun deleteRecord(recordPath: String) {
        withContext(appCoroutineDispatchers.io) {
            val file = File(recordPath)
            if (file.exists()) {
                file.deleteRecursively()
            }
        }
    }
}