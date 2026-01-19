package com.example.data.syncdata.model

import com.example.data.datalocal.model.CategoryEntity
import com.example.data.datalocal.model.NoteEntity

data class SyncPackage(
    val categories: List<CategoryEntity>,
    val notes: List<NoteEntity>
)