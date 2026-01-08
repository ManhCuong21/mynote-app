package com.example.domain.usecase.file

import com.example.core.core.model.ItemRecord
import com.example.data.file.record.RecordFileRepository
import javax.inject.Inject

class RecordFileUseCase @Inject constructor(
    private val recordFileRepository: RecordFileRepository
) {
    suspend fun getListRecord(): List<ItemRecord> = recordFileRepository.getListRecord()

    suspend fun deleteRecord(recordPath: String) = recordFileRepository.deleteRecord(recordPath)
}