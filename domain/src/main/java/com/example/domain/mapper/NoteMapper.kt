package com.example.domain.mapper

import com.example.core.core.model.CategoryUIModel
import com.example.core.core.model.NoteUIModel
import com.example.data.model.NoteEntity

data class NoteParams(
    val categoryNote: CategoryUIModel,
    val titleNote: String,
    val contentNote: String,
    val fileMediaNote: String,
    var colorTitleNote: String,
    var colorContentNote: String,
    var timeNote: Long
)

fun NoteParams.toNoteEntity() = NoteEntity(
    categoryEntity = categoryNote.toCategoryEntity(),
    titleNote = titleNote,
    contentNote = contentNote,
    fileMediaNote = fileMediaNote,
    colorTitleNote = colorTitleNote,
    colorContentNote = colorContentNote,
    timeNote = timeNote
)

fun NoteUIModel.toNoteEntity() = NoteEntity(
    idNote = idNote,
    categoryEntity = categoryNote.toCategoryEntity(),
    titleNote = titleNote,
    contentNote = contentNote,
    fileMediaNote = fileMediaNote,
    colorTitleNote = colorTitleNote,
    colorContentNote = colorContentNote,
    timeNote = timeNote
)

fun NoteEntity.toNoteUIModel() = NoteUIModel(
    idNote = idNote,
    categoryNote = categoryEntity.toCategoryUIModel(),
    titleNote = titleNote,
    contentNote = contentNote,
    fileMediaNote = fileMediaNote,
    colorTitleNote = colorTitleNote,
    colorContentNote = colorContentNote,
    timeNote = timeNote ?: System.currentTimeMillis()
)