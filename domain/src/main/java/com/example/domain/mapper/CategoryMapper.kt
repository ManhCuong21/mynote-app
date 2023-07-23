package com.example.domain.mapper

import com.example.core.core.model.CategoryUIModel
import com.example.data.model.CategoryEntity
import com.example.domain.model.CategoryModel

fun CategoryModel.toCategoryEntity() = CategoryEntity(
    title = title,
    image = image
)

fun CategoryModel.toCategoryUIModel() = CategoryUIModel(
    id = id,
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