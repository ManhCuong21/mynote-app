package com.example.mynote.ui.main.home

import android.os.Parcelable
import com.example.mynote.domain.model.CategoryModel
import com.example.mynote.domain.model.NoteModel
import kotlinx.parcelize.Parcelize

sealed interface HomeAction {
    object GetListCategory : HomeAction
    data class ListCategoryChanged(val list: List<CategoryModel>) : HomeAction
    data class ListNoteChanged(val list: List<NoteModel>) : HomeAction
}

sealed interface HomeSingleEvent {
    sealed interface GetListCategory : HomeSingleEvent {
        data class Success(val list: List<CategoryModel>) : GetListCategory
        data class Failed(val error: Throwable) : GetListCategory
    }
}

@Parcelize
data class HomeUiState(
    val listCategory: List<CategoryModel>,
    val listNote: List<NoteModel>
) : Parcelable {
    companion object {
        val INITIAL = HomeUiState(
            listCategory = listOf(),
            listNote = listOf()
        )
    }
}

fun buildHomeUiState(
    listCategory: List<CategoryModel>,
    listNote: List<NoteModel>
): HomeUiState = HomeUiState(
    listCategory = listCategory,
    listNote = listNote
)