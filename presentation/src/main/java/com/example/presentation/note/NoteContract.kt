package com.example.presentation.note

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface NoteAction {
    object UpdateListImage : NoteAction
    object UpdateListRecord : NoteAction
    data class TitleNoteChanged(val titleNote: String) : NoteAction
    data class ContentNoteChanged(val contentNote: String) : NoteAction
    data class CategoryNoteChanged(val categoryNote: Int) : NoteAction
    data class FileMediaNoteChanged(val fileMediaNote: String) : NoteAction
    data class ColorNoteChanged(val indexColor: Int) : NoteAction
    object InsertNote : NoteAction
}

sealed interface NoteSingleEvent {
    object UpdateListImage : NoteSingleEvent
    object UpdateListRecord : NoteSingleEvent
    sealed interface SaveNote : NoteSingleEvent {
        object Success : SaveNote
        data class Failed(val error: Throwable) : SaveNote
    }}

@Parcelize
data class NoteUiState(
    val titleNote: String?,
    val contentNote: String?,
    val categoryId: Int?,
    val fileMediaNote: String?,
    var colorNote: Int?
) : Parcelable {
    companion object {
        val INITIAL = NoteUiState(
            titleNote = null,
            contentNote = null,
            categoryId = null,
            fileMediaNote = null,
            colorNote = null
        )
    }
}

fun buildNoteUiState(
    titleNote: String,
    contentNote: String,
    categoryId: Int?,
    fileMediaNote: String?,
    colorNote: Int?,
): NoteUiState = NoteUiState(
    titleNote = titleNote,
    contentNote = contentNote,
    categoryId = categoryId,
    fileMediaNote = fileMediaNote,
    colorNote = colorNote,
)