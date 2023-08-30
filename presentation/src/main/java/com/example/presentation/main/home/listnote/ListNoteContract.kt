package com.example.presentation.main.home.listnote

import android.os.Parcelable
import com.example.core.core.model.CategoryModel
import com.example.core.core.model.NoteModel
import kotlinx.parcelize.Parcelize

sealed interface ListNoteAction {
    data class GetListNote(val category: CategoryModel) : ListNoteAction
    data class ChangeCategoryNote(val noteModel: NoteModel, val category: CategoryModel) :
        ListNoteAction

    data class DeleteNote(val noteModel: NoteModel) : ListNoteAction
}

sealed interface ListNoteSingleEvent {
    data class GetListNoteSuccess(val listNote: List<NoteModel>) : ListNoteSingleEvent
    object UpdateNote : ListNoteSingleEvent
    object DeleteNoteSuccess : ListNoteSingleEvent
    data class SingleEventFailed(val error: Throwable) : ListNoteSingleEvent
}

@Parcelize
data class ListNoteUiState(
    val listCategory: List<CategoryModel>,
    val category: CategoryModel?,
    val listNote: List<NoteModel>
) : Parcelable {
    companion object {
        val INITIAL = ListNoteUiState(
            listCategory = listOf(),
            category = null,
            listNote = listOf()
        )
    }
}