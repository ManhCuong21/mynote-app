package com.example.presentation.main.home.listnote

import android.os.Parcelable
import com.example.core.core.model.CategoryUIModel
import com.example.core.core.model.NoteUIModel
import kotlinx.parcelize.Parcelize

sealed interface ListNoteAction {
    data class GetListNote(val category: CategoryUIModel) : ListNoteAction
}

sealed interface ListNoteSingleEvent {
    sealed interface GetListNote : ListNoteSingleEvent {
        data class Success(val listNote: List<NoteUIModel>) : GetListNote
        data class Failed(val error: Throwable) : GetListNote
    }
}

@Parcelize
data class ListNoteUiState(
    val category: CategoryUIModel?,
    val listNote: List<NoteUIModel>
) : Parcelable {
    companion object {
        val INITIAL = ListNoteUiState(
            category = null,
            listNote = listOf()
        )
    }
}