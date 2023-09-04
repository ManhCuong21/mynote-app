package com.example.data.datalocal.database

import com.example.data.datalocal.dao.AppDAO
import com.example.data.datalocal.model.CategoryEntity
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import javax.inject.Inject

interface CategoryDatabase {
    suspend fun insertCategory(category: CategoryEntity): Result<Unit, Throwable>
    suspend fun readAllCategory(): Result<List<CategoryEntity>, Throwable>
    suspend fun readCategoryWithId(categoryId: Int): Result<CategoryEntity, Throwable>
    suspend fun updateCategory(category: CategoryEntity): Result<Unit, Throwable>
    suspend fun deleteCategory(category: CategoryEntity): Result<Unit, Throwable>
}

class CategoryDatabaseImpl @Inject constructor(appDAO: AppDAO) : CategoryDatabase {
    private val categoryDAO = appDAO.categoryDao()

    override suspend fun insertCategory(category: CategoryEntity): Result<Unit, Throwable> =
        runCatching {
            categoryDAO.insertCategory(category)
        }

    override suspend fun readAllCategory(): Result<List<CategoryEntity>, Throwable> =
        runCatching {
            categoryDAO.readAllCategory()
        }

    override suspend fun readCategoryWithId(categoryId: Int): Result<CategoryEntity, Throwable> =
        runCatching {
            categoryDAO.readCategoryWithId(categoryId)
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