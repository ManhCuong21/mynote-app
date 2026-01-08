package com.example.presentation.category

import android.os.Parcelable
import com.example.core.core.model.CategoryModel
import kotlinx.parcelize.Parcelize

sealed interface CategoryAction {
    data class TitleCategoryChanged(val title: String) : CategoryAction
    data class ImageCategoryChanged(val image: String) : CategoryAction
    data class SecurityCategoryChanged(val securityCategory: Boolean) : CategoryAction
    data object InsertCategory : CategoryAction
    data class UpdateCategory(val categoryModel: CategoryModel) : CategoryAction
}

sealed interface AddCategorySingleEvent {
    sealed interface SaveCategory : AddCategorySingleEvent {
        data object Success : SaveCategory
        data class Failed(val error: Throwable) : SaveCategory
    }
}

@Parcelize
data class CategoryUiState(
    val title: String,
    val image: String,
    val securityCategory: Boolean
) : Parcelable {
    companion object {
        val INITIAL = CategoryUiState(
            title = "Ex",
            image = "icon_ex",
            securityCategory = false
        )
    }
}

fun buildCategoryUiState(
    title: String,
    image: String,
    securityCategory: Boolean
): CategoryUiState = CategoryUiState(
    title = title,
    image = image,
    securityCategory = securityCategory
)