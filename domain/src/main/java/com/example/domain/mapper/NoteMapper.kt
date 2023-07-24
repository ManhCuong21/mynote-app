package com.example.domain.mapper

import com.example.core.core.model.NoteUIModel
import com.example.data.model.NoteEntity

fun NoteUIModel.toNoteEntity() = NoteEntity(
    categoryId = categoryId,
    titleNote = titleNote,
    contentNote = contentNote,
    fileMediaNote = fileMediaNote,
    colorNote = colorNote,
    timeNote = timeNote
)