package com.example.presentation.addnote

import android.os.Parcelable
import com.example.core.core.model.CategoryUIModel
import kotlinx.parcelize.Parcelize

sealed interface NoteAction {
    object UpdateListImage : NoteAction
    object UpdateListRecord : NoteAction
}

sealed interface NoteSingleEvent {
    object UpdateListImage : NoteSingleEvent
    object UpdateListRecord : NoteSingleEvent
}

@Parcelize
data class NoteUiState(
    val listCategory: List<CategoryUIModel>
) : Parcelable {
    companion object {
        val INITIAL = NoteUiState(
            listCategory = listOf()
        )
    }
}

fun buildNoteUiState(
    listCategory: List<CategoryUIModel>
): NoteUiState = NoteUiState(
    listCategory = listCategory
)

data class ItemChooseColor(val colorTitle: Int, val colorContent: Int)