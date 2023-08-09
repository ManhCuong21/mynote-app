package com.example.presentation.main.home.listnote

import android.os.Parcelable
import com.example.core.core.model.CategoryUIModel
import com.example.core.core.model.NoteUIModel
import kotlinx.parcelize.Parcelize

sealed interface ListNoteAction {
    data class GetListNote(val category: CategoryUIModel) : ListNoteAction
    data class ChangeCategoryNote(val noteUIModel: NoteUIModel, val category: CategoryUIModel) :
        ListNoteAction

    data class DeleteNote(val noteUIModel: NoteUIModel) : ListNoteAction
}

sealed interface ListNoteSingleEvent {
    sealed interface GetListNote : ListNoteSingleEvent {
        data class Success(val listNote: List<NoteUIModel>) : GetListNote
        data class Failed(val error: Throwable) : GetListNote
    }

    object UpdateNote : ListNoteSingleEvent
    object DeleteNote : ListNoteSingleEvent
}

@Parcelize
data class ListNoteUiState(
    val listCategory: List<CategoryUIModel>,
    val category: CategoryUIModel?,
    val listNote: List<NoteUIModel>
) : Parcelable {
    companion object {
        val INITIAL = ListNoteUiState(
            listCategory = listOf(),
            category = null,
            listNote = listOf()
        )
    }
}