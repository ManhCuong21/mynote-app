package com.example.presentation.main.home

import android.os.Parcelable
import com.example.core.core.model.CategoryUIModel
import com.example.core.core.model.ListDialogItem
import com.example.core.core.model.NoteUIModel
import kotlinx.parcelize.Parcelize

sealed interface HomeAction {
    object GetListCategory : HomeAction
    data class ListCategoryChanged(val list: List<CategoryUIModel>) : HomeAction
    data class ListNoteChanged(val list: List<NoteUIModel>) : HomeAction
}

sealed interface HomeSingleEvent {
    sealed interface GetListCategory : HomeSingleEvent {
        data class Success(val list: List<CategoryUIModel>) : GetListCategory
        data class Failed(val error: Throwable) : GetListCategory
    }
}

@Parcelize
data class HomeUiState(
    val listCategory: List<CategoryUIModel>,
    val listNote: List<NoteUIModel>
) : Parcelable {
    companion object {
        val INITIAL = HomeUiState(
            listCategory = listOf(),
            listNote = listOf()
        )
    }
}

fun buildHomeUiState(
    listCategory: List<CategoryUIModel>,
    listNote: List<NoteUIModel>
): HomeUiState = HomeUiState(
    listCategory = listCategory,
    listNote = listNote
)

internal fun CategoryUIModel.toListDialogItem() = ListDialogItem(
    title = title,
    image = image
)