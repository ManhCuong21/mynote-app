package com.example.presentation.note

import android.os.Parcelable
import com.example.core.core.model.CategoryUIModel
import com.example.core.core.model.NoteUIModel
import kotlinx.parcelize.Parcelize

sealed interface NoteAction {
    object UpdateListImage : NoteAction
    object UpdateListRecord : NoteAction
    data class TitleNoteChanged(val titleNote: String) : NoteAction
    data class ContentNoteChanged(val contentNote: String) : NoteAction
    data class CategoryNoteChanged(val categoryNote: CategoryUIModel) : NoteAction
    data class FileMediaNoteChanged(val fileMediaNote: String) : NoteAction
    data class ColorTitleNoteChanged(val colorTitleNote: String) : NoteAction
    data class ColorContentNoteChanged(val colorContentNote: String) : NoteAction
    object InsertNote : NoteAction
    data class UpdateNote(val noteModel: NoteUIModel) : NoteAction
}

sealed interface NoteSingleEvent {
    object UpdateListImage : NoteSingleEvent
    object UpdateListRecord : NoteSingleEvent
    sealed interface SaveNote : NoteSingleEvent {
        object Success : SaveNote
        data class Failed(val error: Throwable) : SaveNote
    }
}

@Parcelize
data class NoteUiState(
    val titleNote: String?,
    val contentNote: String?,
    val categoryNote: CategoryUIModel,
    val fileMediaNote: String?,
    val colorTitleNote: String?,
    val colorContentNote: String?
) : Parcelable {
    companion object {
        val INITIAL = NoteUiState(
            titleNote = null,
            contentNote = null,
            categoryNote = CategoryUIModel(-1, "", 0),
            fileMediaNote = null,
            colorTitleNote = null,
            colorContentNote = null
        )
    }
}

fun buildNoteUiState(
    titleNote: String,
    contentNote: String,
    categoryNote: CategoryUIModel,
    fileMediaNote: String?,
    colorTitleNote: String?,
    colorContentNote: String?
): NoteUiState = NoteUiState(
    titleNote = titleNote,
    contentNote = contentNote,
    categoryNote = categoryNote,
    fileMediaNote = fileMediaNote,
    colorTitleNote = colorTitleNote,
    colorContentNote = colorContentNote
)