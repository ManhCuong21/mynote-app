package com.example.domain.usecase.file

import androidx.fragment.app.FragmentActivity
import com.example.core.core.model.ItemRecord
import com.example.data.file.record.RecordFileRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import java.io.File
import javax.inject.Inject

class RecordFileUseCase @Inject constructor(
    private val recordFileRepository: RecordFileRepository
) {
    suspend fun saveAmplitude(file: File, amplitudes: List<Float>) =
        recordFileRepository.saveAmplitude(file, amplitudes)

    suspend fun saveRecordToDirectory(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ): Result<Unit, Throwable> = runCatching {
        recordFileRepository.saveRecordToDirectory(fragmentActivity, directoryName)
    }

    suspend fun saveRecordFromDirectoryToTemp(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ): Result<Unit, Throwable> = runCatching {
        recordFileRepository.saveRecordFromDirectoryToTemp(fragmentActivity, directoryName)
    }

    suspend fun readRecord(fragmentActivity: FragmentActivity): List<ItemRecord> =
        recordFileRepository.readRecord(fragmentActivity)

    suspend fun deleteRecord(recordPath: String) = recordFileRepository.deleteRecord(recordPath)
}