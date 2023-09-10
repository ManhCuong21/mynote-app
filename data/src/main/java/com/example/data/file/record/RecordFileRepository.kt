package com.example.data.file.record

import androidx.fragment.app.FragmentActivity
import com.example.core.core.model.ItemRecord

interface RecordFileRepository {
    fun saveRecordToDirectory(fragmentActivity: FragmentActivity, directoryName: String)

    suspend fun readRecord(fragmentActivity: FragmentActivity): List<ItemRecord>

    suspend fun deleteRecord(pathRecord: String)
}