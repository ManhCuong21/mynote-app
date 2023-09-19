package com.example.domain.usecase.data

import com.example.core.core.external.AppConstants.TYPE_REMOTE
import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.model.CategoryModel
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.data.datalocal.repository.CategoryLocalRepository
import com.example.data.dataremote.repository.CategoryRemoteRepository
import com.example.domain.mapper.CategoryParams
import com.example.domain.mapper.toCategoryEntity
import com.example.domain.mapper.toCategoryRemote
import com.example.domain.mapper.toListCategory
import com.example.domain.mapper.toListCategoryModel
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.runCatching
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoryUseCase @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val sharedPrefersManager: SharedPrefersManager,
    private val categoryRepository: CategoryLocalRepository,
    private val categoryRemoteRepository: CategoryRemoteRepository
) {
    suspend fun insertCategory(category: CategoryParams): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            if (!sharedPrefersManager.userEmail.isNullOrEmpty()) {
                categoryRemoteRepository.insertCategory(category.toCategoryRemote())
            } else {
                categoryRepository.insertCategory(category.toCategoryEntity())
            }
        }

    suspend fun readAllCategory(): Result<List<CategoryModel>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            if (!sharedPrefersManager.userEmail.isNullOrEmpty()) {
                runCatching {
                    val listRemoteCall = async {
                        categoryRemoteRepository.readAllCategory().map { flow ->
                            flow.first().toListCategoryModel()
                        }
                    }
                    val listLocalCall = async {
                        categoryRepository.readAllCategory().map { it.toListCategory() }
                    }
                    val listRemote = listRemoteCall.await()
                    val listLocal = listLocalCall.await()
                    val listCategory = arrayListOf<CategoryModel>()
                    val listCategoryRemote = when (listRemote) {
                        is Ok -> {
                            listRemote.value
                        }

                        is Err -> {
                            listOf()
                        }
                    }
                    val listCategoryLocal = when (listLocal) {
                        is Ok -> {
                            listLocal.value
                        }

                        is Err -> {
                            listOf()
                        }
                    }
                    listCategory.addAll(listCategoryLocal)
                    listCategory.addAll(listCategoryRemote)
                    listCategory
                }
            } else {
                categoryRepository.readAllCategory().map { it.toListCategory() }
            }
        }

    suspend fun updateCategory(category: CategoryModel): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            if (!sharedPrefersManager.userEmail.isNullOrEmpty()) {
                categoryRemoteRepository.updateCategory(category.toCategoryRemote())
            } else {
                categoryRepository.updateCategory(category.toCategoryEntity())
            }
        }

    suspend fun deleteCategory(category: CategoryModel): Result<Unit, Throwable> =
        if (!sharedPrefersManager.userEmail.isNullOrEmpty() && category.typeCategory == TYPE_REMOTE) {
            categoryRemoteRepository.deleteCategory(category.toCategoryRemote())
        } else {
            categoryRepository.deleteCategory(category.toCategoryEntity())
        }
}