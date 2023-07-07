package com.example.mynote.domain.repository

import com.example.mynote.core.external.AppCoroutineDispatchers
import com.example.mynote.data.database.CategoryDatabase
import com.example.mynote.domain.mapper.toCategoryEntity
import com.example.mynote.domain.mapper.toListCategoryModel
import com.example.mynote.domain.model.CategoryModel
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface CategoryRepository {
    suspend fun insertCategory(category: CategoryModel): Result<Unit, Throwable>
    suspend fun readAllCategory(): Result<List<CategoryModel>, Throwable>
    suspend fun updateCategory(category: CategoryModel): Result<Unit, Throwable>
    suspend fun deleteCategory(category: CategoryModel): Result<Unit, Throwable>
}

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDatabase: CategoryDatabase,
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) : CategoryRepository {
    override suspend fun insertCategory(category: CategoryModel): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            categoryDatabase.insertCategory(category.toCategoryEntity())
        }

    override suspend fun readAllCategory(): Result<List<CategoryModel>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            categoryDatabase.readAllCategory().map { it.toListCategoryModel() }

        }

    override suspend fun updateCategory(category: CategoryModel) =
        withContext(appCoroutineDispatchers.io) {
            categoryDatabase.updateCategory(category.toCategoryEntity())
        }

    override suspend fun deleteCategory(category: CategoryModel) =
        withContext(appCoroutineDispatchers.io) {
            categoryDatabase.deleteCategory(category.toCategoryEntity())
        }
}