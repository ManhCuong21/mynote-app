package com.example.presentation.main.listcategory

import com.example.core.core.model.CategoryUIModel

sealed interface ListCategoryAction {
    object GetListCategory : ListCategoryAction
    data class DeleteCategory(val categoryModel: CategoryUIModel) : ListCategoryAction
}

sealed interface ListCategorySingleEvent {
    data class GetListCategorySuccess(val list: List<CategoryUIModel>) : ListCategorySingleEvent
    object DeleteCategorySuccess : ListCategorySingleEvent
    data class SingleEventFailed(val error: Throwable) : ListCategorySingleEvent
}