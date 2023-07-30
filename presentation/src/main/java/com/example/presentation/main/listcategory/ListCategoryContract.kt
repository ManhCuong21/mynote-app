package com.example.presentation.main.listcategory

import com.example.core.core.model.CategoryUIModel

sealed interface ListCategoryAction {
    object GetListCategory : ListCategoryAction
}

sealed interface ListCategorySingleEvent {
    sealed interface GetListCategory : ListCategorySingleEvent {
        data class Success(val list: List<CategoryUIModel>) : GetListCategory
        data class Failed(val error: Throwable) : GetListCategory
    }
}