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

    override suspend fun getListRecord(): List<ItemRecord> = withContext(appCoroutineDispatchers.io) {
        val tempDir = fileRepository.createOrGetDirectory("Temp")

        // Sử dụng listFiles thay vì walkTopDown nếu bạn chỉ cần 1 cấp thư mục (nhanh hơn)
        tempDir.listFiles()
            ?.filter { it.isFile && it.canRead() && it.extension.equals("mp4", true) }
            ?.map { file ->
                ItemRecord(
                    directoryPath = tempDir.absolutePath,
                    recordPath = file.absolutePath
                )
            } ?: emptyList()
    }

    override suspend fun deleteRecord(recordPath: String) {
        withContext(appCoroutineDispatchers.io) {
            val file = File(recordPath)
            if (file.exists()) file.delete()
        }
    }
}