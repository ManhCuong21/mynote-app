package com.example.domain.mapper

import com.example.core.core.model.CategoryUIModel
import com.example.data.model.CategoryEntity

data class CategoryParams(val title: String, val image: Int)

fun CategoryParams.toCategoryEntity() = CategoryEntity(
    titleCategory = title,
    imageCategory = image
)

fun CategoryUIModel.toCategoryEntity() = CategoryEntity(
    idCategory = idCategory,
    titleCategory = titleCategory,
    imageCategory = imageCategory
)

internal fun List<CategoryEntity>.toListCategoryModel(): List<CategoryUIModel> =
    this.map { it.toCategoryUIModel() }

fun CategoryEntity.toCategoryUIModel() = CategoryUIModel(
    idCategory = idCategory,
    titleCategory = titleCategory,
    imageCategory = imageCategory
)