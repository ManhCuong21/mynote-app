package com.example.mynote.domain.mapper

import com.example.mynote.data.model.CategoryEntity
import com.example.mynote.domain.model.CategoryModel
import com.example.mynote.ui.dialog.list.ListDialogItem

fun CategoryModel.toCategoryEntity() = CategoryEntity(
    title = title,
    image = image
)

internal fun List<CategoryEntity>.toListCategoryModel(): List<CategoryModel> =
    this.map { it.toCategoryModel() }

private fun CategoryEntity.toCategoryModel() = CategoryModel(
    id = id,
    title = title,
    image = image
)

internal fun CategoryModel.toListDialogItem() = ListDialogItem(
    title = title,
    image = image
)