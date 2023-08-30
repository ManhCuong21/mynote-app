package com.example.presentation.category

import android.os.Parcelable
import com.example.core.core.model.CategoryModel
import com.example.presentation.R
import kotlinx.parcelize.Parcelize

sealed interface CategoryAction {
    data class TitleCategoryChanged(val value: String) : CategoryAction
    data class ImageCategoryChanged(val value: Int) : CategoryAction
    object InsertCategory : CategoryAction
    data class UpdateCategory(val categoryModel: CategoryModel) : CategoryAction
}

sealed interface AddCategorySingleEvent {
    sealed interface SaveCategory : AddCategorySingleEvent {
        object Success : SaveCategory
        data class Failed(val error: Throwable) : SaveCategory
    }
}

@Parcelize
data class CategoryUiState(
    val title: String,
    val image: Int
) : Parcelable {
    companion object {
        val INITIAL = CategoryUiState(
            title = "Ex",
            image = R.drawable.icon_ex
        )
    }
}

fun buildCategoryUiState(
    title: String,
    image: Int,
): CategoryUiState = CategoryUiState(
    title = title,
    image = image
)