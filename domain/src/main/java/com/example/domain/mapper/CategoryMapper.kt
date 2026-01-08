package com.example.domain.mapper

import com.example.core.core.model.CategoryModel
import com.example.data.datalocal.model.CategoryEntity

data class CategoryParams(val title: String, val image: String, val security: Boolean)

fun CategoryParams.toCategoryEntity() = CategoryEntity(
    titleCategory = title,
    imageCategory = image,
    securityCategory = security
)

fun CategoryModel.toCategoryEntity() = CategoryEntity(
    idCategory = idCategory,
    titleCategory = titleCategory,
    imageCategory = imageCategory,
    securityCategory = securityCategory
)

internal fun List<CategoryEntity>.toListCategory(): List<CategoryModel> =
    this.map { it.toCategory() }

fun CategoryEntity.toCategory() = CategoryModel(
    idCategory = idCategory,
    titleCategory = titleCategory,
    imageCategory = imageCategory,
    securityCategory = securityCategory
)