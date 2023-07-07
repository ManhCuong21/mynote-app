package com.example.mynote.ui.usecase

import com.example.mynote.domain.model.CategoryModel
import com.example.mynote.domain.repository.CategoryRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.binding
import javax.inject.Inject

class CategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend fun insertCategory(category: CategoryModel): Result<Unit, Throwable> = binding {
        categoryRepository.insertCategory(category).bind()
    }

    suspend fun readAllCategory(): Result<List<CategoryModel>, Throwable> =
        categoryRepository.readAllCategory()

    suspend fun updateCategory(category: CategoryModel): Result<Unit, Throwable> =
        categoryRepository.updateCategory(category)

    suspend fun deleteCategory(category: CategoryModel): Result<Unit, Throwable> =
        categoryRepository.deleteCategory(category)
}