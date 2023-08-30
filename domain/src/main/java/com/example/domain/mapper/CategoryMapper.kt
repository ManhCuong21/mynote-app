package com.example.domain.mapper

import com.example.core.core.model.CategoryModel
import com.example.data.model.CategoryEntity

data class CategoryParams(val title: String, val image: Int)

fun CategoryParams.toCategoryEntity() = CategoryEntity(
    titleCategory = title,
    imageCategory = image
)

fun CategoryModel.toCategoryEntity() = CategoryEntity(
    idCategory = idCategory,
    titleCategory = titleCategory,
    imageCategory = imageCategory
)

internal fun List<CategoryEntity>.toListCategoryModel(): List<CategoryModel> =
    this.map { it.toCategoryModel() }

fun CategoryEntity.toCategoryModel() = CategoryModel(
    idCategory = idCategory,
    titleCategory = titleCategory,
    imageCategory = imageCategory
)