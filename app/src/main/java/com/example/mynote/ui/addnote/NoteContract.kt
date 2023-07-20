package com.example.mynote.ui.addnote

import android.os.Parcelable
import com.example.mynote.domain.model.CategoryModel
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
    val listCategory: List<CategoryModel>
) : Parcelable {
    companion object {
        val INITIAL = NoteUiState(
            listCategory = listOf()
        )
    }
}

fun buildNoteUiState(
    listCategory: List<CategoryModel>
): NoteUiState = NoteUiState(
    listCategory = listCategory
)

data class ItemChooseColor(val colorTitle: Int, val colorContent: Int)