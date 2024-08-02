package com.example.data.file.record

import androidx.fragment.app.FragmentActivity
import com.example.core.core.model.ItemRecord
import java.io.File

interface RecordFileRepository {
    suspend fun saveAmplitude(file: File, amplitudes: List<Float>)
    suspend fun saveRecordToDirectory(fragmentActivity: FragmentActivity, directoryPath: String)
    suspend fun saveRecordFromDirectoryToTemp(fragmentActivity: FragmentActivity, directoryName: String)
    suspend fun readRecord(fragmentActivity: FragmentActivity): List<ItemRecord>
    suspend fun deleteRecord(recordPath: String)
}