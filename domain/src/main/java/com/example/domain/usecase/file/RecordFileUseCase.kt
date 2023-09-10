package com.example.domain.usecase.file

import androidx.fragment.app.FragmentActivity
import com.example.core.core.model.ItemRecord
import com.example.data.file.record.RecordFileRepository
import javax.inject.Inject

class RecordFileUseCase @Inject constructor(
    private val recordFileRepository: RecordFileRepository
) {
    fun saveRecordToDirectory(fragmentActivity: FragmentActivity, directoryName: String) =
        recordFileRepository.saveRecordToDirectory(fragmentActivity, directoryName)

    suspend fun readRecord(fragmentActivity: FragmentActivity): List<ItemRecord> =
        recordFileRepository.readRecord(fragmentActivity)

    suspend fun deleteRecord(pathRecord: String) = recordFileRepository.deleteRecord(pathRecord)
}