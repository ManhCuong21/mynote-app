package com.example.data.file.record

import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.model.ItemRecord
import com.example.data.file.file.FileRepository
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class RecordFileRepositoryImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val fileRepository: FileRepository
) : RecordFileRepository {

    override suspend fun getListRecord(): List<ItemRecord> {
        return withContext(appCoroutineDispatchers.io) {
            val tempDir = fileRepository.createOrGetDirectory("Temp")

            if (!tempDir.exists() || !tempDir.isDirectory) return@withContext emptyList()

            tempDir.walkTopDown()
                .maxDepth(1)
                .filter { file ->
                    file.isFile && file.canRead() && file.extension.equalsIgnoreCase("mp4")
                }
                .map { file ->
                    ItemRecord(
                        directoryPath = file.parentFile?.absolutePath ?: tempDir.absolutePath,
                        recordPath = file.absolutePath
                    )
                }
                .toList()
        }
    }

    override suspend fun deleteRecord(recordPath: String) {
        withContext(appCoroutineDispatchers.io) {
            val file = File(recordPath)
            if (file.exists() && file.isFile) {
                file.delete()
            }
        }
    }

    private fun String.equalsIgnoreCase(other: String): Boolean = this.equals(other, ignoreCase = true)
}