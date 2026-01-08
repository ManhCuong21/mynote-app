package com.example.data.file.record

import com.example.core.core.model.ItemRecord

interface RecordFileRepository {
    suspend fun getListRecord(): List<ItemRecord>
    suspend fun deleteRecord(recordPath: String)
}