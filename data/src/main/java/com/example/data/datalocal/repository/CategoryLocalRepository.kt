package com.example.data.datalocal.repository

import com.example.core.core.external.AppCoroutineDispatchers
import com.example.data.datalocal.database.CategoryDatabase
import com.example.data.datalocal.model.CategoryEntity
import com.github.michaelbull.result.Result
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface CategoryLocalRepository {
    suspend fun insertCategory(category: CategoryEntity): Result<Unit, Throwable>
    suspend fun readAllCategory(): Result<List<CategoryEntity>, Throwable>
    suspend fun updateCategory(category: CategoryEntity): Result<Unit, Throwable>
    suspend fun deleteCategory(category: CategoryEntity): Result<Unit, Throwable>
}

class CategoryLocalRepositoryImpl @Inject constructor(
    private val categoryDatabase: CategoryDatabase,
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) : CategoryLocalRepository {
    override suspend fun insertCategory(category: CategoryEntity): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            categoryDatabase.insertCategory(category)
        }

    override suspend fun readAllCategory(): Result<List<CategoryEntity>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            categoryDatabase.readAllCategory()
        }

    override suspend fun updateCategory(category: CategoryEntity) =
        withContext(appCoroutineDispatchers.io) {
            categoryDatabase.updateCategory(category)
        }

    override suspend fun deleteCategory(category: CategoryEntity) =
        withContext(appCoroutineDispatchers.io) {
            categoryDatabase.deleteCategory(category)
        }
}