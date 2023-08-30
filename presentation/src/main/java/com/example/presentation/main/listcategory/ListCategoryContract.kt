package com.example.presentation.main.listcategory

import com.example.core.core.model.CategoryModel

sealed interface ListCategoryAction {
    object GetListCategory : ListCategoryAction
    data class DeleteCategory(val categoryModel: CategoryModel) : ListCategoryAction
}

sealed interface ListCategorySingleEvent {
    data class GetListCategorySuccess(val list: List<CategoryModel>) : ListCategorySingleEvent
    object DeleteCategorySuccess : ListCategorySingleEvent
    data class SingleEventFailed(val error: Throwable) : ListCategorySingleEvent
}