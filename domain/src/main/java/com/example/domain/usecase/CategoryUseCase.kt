package com.example.domain.usecase

import com.example.core.core.model.CategoryModel
import com.example.data.repository.CategoryRepository
import com.example.domain.mapper.CategoryParams
import com.example.domain.mapper.toCategoryEntity
import com.example.domain.mapper.toCategoryModel
import com.example.domain.mapper.toListCategoryModel
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import javax.inject.Inject

class CategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend fun insertCategory(category: CategoryParams): Result<Unit, Throwable> =
        categoryRepository.insertCategory(category.toCategoryEntity())


    suspend fun readAllCategory(): Result<List<CategoryModel>, Throwable> =
        categoryRepository.readAllCategory().map { it.toListCategoryModel() }

    suspend fun readCategoryWithId(categoryId: Int): Result<CategoryModel, Throwable> =
        categoryRepository.readCategoryWithId(categoryId).map { it.toCategoryModel() }


    suspend fun updateCategory(category: CategoryModel): Result<Unit, Throwable> =
        categoryRepository.updateCategory(category.toCategoryEntity())

    suspend fun deleteCategory(category: CategoryModel): Result<Unit, Throwable> =
        categoryRepository.deleteCategory(category.toCategoryEntity())
}