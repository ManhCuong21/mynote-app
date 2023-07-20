package com.example.mynote.core.file.record

import android.graphics.Bitmap
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.example.mynote.domain.model.ItemRecord

interface RecordFile {
    fun saveRecordToFile(fragmentActivity: FragmentActivity, pathChild: Uri, bitmap: Bitmap)
    suspend fun readRecordFromFile(
        fragmentActivity: FragmentActivity,
        pathFile: String
    ): List<ItemRecord>

    suspend fun deleteRecordFromFile(
        pathRecord: String
    )
}