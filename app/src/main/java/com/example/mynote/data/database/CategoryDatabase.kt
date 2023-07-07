package com.example.mynote.data.database

import com.example.mynote.data.dao.AppDAO
import com.example.mynote.data.model.CategoryEntity
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import javax.inject.Inject

interface CategoryDatabase {
    suspend fun insertCategory(category: CategoryEntity): Result<Unit, Throwable>
    suspend fun readAllCategory(): Result<List<CategoryEntity>, Throwable>
    suspend fun updateCategory(category: CategoryEntity): Result<Unit, Throwable>
    suspend fun deleteCategory(category: CategoryEntity): Result<Unit, Throwable>
}

class CategoryDatabaseImpl @Inject constructor(
    private val appDAO: AppDAO
) : CategoryDatabase {
    private val categoryDAO = appDAO.categoryDao()

    override suspend fun insertCategory(category: CategoryEntity): Result<Unit, Throwable> =
        runCatching {
            categoryDAO.insertCategory(category)
        }

    override suspend fun readAllCategory(): Result<List<CategoryEntity>, Throwable> =
        runCatching {
            categoryDAO.readAllCategory()
        }

    override suspend fun updateCategory(category: CategoryEntity): Result<Unit, Throwable> =
        runCatching {
            categoryDAO.updateCategory(category)
        }

    override suspend fun deleteCategory(category: CategoryEntity): Result<Unit, Throwable> =
        runCatching {
            categoryDAO.deleteCategory(category)
        }

}