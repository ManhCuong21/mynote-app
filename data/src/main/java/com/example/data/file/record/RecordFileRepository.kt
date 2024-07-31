package com.example.data.file.record

import androidx.fragment.app.FragmentActivity
import com.example.core.core.model.ItemRecord
import java.io.File

interface RecordFileRepository {
    fun saveAmplitude(file: File, amplitudes: List<Float>)
    fun saveRecordToDirectory(fragmentActivity: FragmentActivity, pathDirectory: String)
    suspend fun readRecord(fragmentActivity: FragmentActivity): List<ItemRecord>
    suspend fun deleteRecord(pathRecord: String)
}