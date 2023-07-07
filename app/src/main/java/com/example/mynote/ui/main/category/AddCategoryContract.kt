package com.example.mynote.ui.main.category

import android.os.Parcelable
import com.example.mynote.R
import kotlinx.parcelize.Parcelize

sealed interface AddCategoryAction {
    data class TitleCategoryChanged(val value: String) : AddCategoryAction
    data class ImageCategoryChanged(val value: Int) : AddCategoryAction
    object SaveCategory : AddCategoryAction

}

sealed interface AddCategorySingleEvent {
    sealed interface SaveCategory : AddCategorySingleEvent {
        object Success : SaveCategory
        data class Failed(val error: Throwable) : SaveCategory
    }
}

@Parcelize
data class AddCategoryUiState(
    val title: String,
    val image: Int
) : Parcelable {
    companion object {
        val INITIAL = AddCategoryUiState(
            title = "Ex",
            image = R.drawable.icon_ex
        )
    }
}

fun buildAddCategoryUiState(
    title: String,
    image: Int,
): AddCategoryUiState = AddCategoryUiState(
    title = title,
    image = image
)

data class ItemCategory(val title: String, val image: Int)