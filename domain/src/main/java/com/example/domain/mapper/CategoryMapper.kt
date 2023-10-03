package com.example.domain.mapper

import com.example.core.core.model.CategoryModel
import com.example.data.datalocal.model.CategoryEntity
import com.example.data.dataremote.model.CategoryRemote

data class CategoryParams(val title: String, val image: String)

fun CategoryParams.toCategoryEntity() = CategoryEntity(
    titleCategory = title,
    imageCategory = image
)

fun CategoryParams.toCategoryRemote() = CategoryRemote(
    titleCategory = title,
    imageCategory = image
)

fun CategoryModel.toCategoryEntity() = CategoryEntity(
    idCategory = idCategory,
    titleCategory = titleCategory,
    imageCategory = imageCategory
)

fun CategoryModel.toCategoryRemote() = CategoryRemote(
    idCategory = idCategory,
    titleCategory = titleCategory,
    imageCategory = imageCategory
)

internal fun List<CategoryEntity>.toListCategory(): List<CategoryModel> =
    this.map { it.toCategory() }

internal fun List<CategoryRemote>.toListCategoryModel(): List<CategoryModel> =
    this.map { it.toCategoryModel() }

fun CategoryEntity.toCategory() = CategoryModel(
    idCategory = idCategory,
    titleCategory = titleCategory,
    imageCategory = imageCategory,
    typeCategory = typeCategory
)

fun CategoryRemote.toCategoryModel() = CategoryModel(
    idCategory = idCategory,
    titleCategory = titleCategory.orEmpty(),
    imageCategory = imageCategory.orEmpty(),
    typeCategory = typeCategory
)