package com.example.domain.usecase.data

import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.model.CategoryModel
import com.example.data.datalocal.repository.CategoryRepository
import com.example.domain.mapper.CategoryParams
import com.example.domain.mapper.toCategoryEntity
import com.example.domain.mapper.toListCategory
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoryUseCase @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val categoryRepository: CategoryRepository
) {
    suspend fun insertCategory(category: CategoryParams): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            categoryRepository.insertCategory(category.toCategoryEntity())
        }

    suspend fun readAllCategory(): Result<List<CategoryModel>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            categoryRepository.readAllCategory().map { it.toListCategory() }
        }

    suspend fun updateCategory(category: CategoryModel): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            categoryRepository.updateCategory(category.toCategoryEntity())
        }

    suspend fun deleteCategory(category: CategoryModel): Result<Unit, Throwable> =
        categoryRepository.deleteCategory(category.toCategoryEntity())
}