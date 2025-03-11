package com.example.data.dataremote.repository

import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.external.throwException
import com.example.data.dataremote.model.CategoryRemote
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface CategoryRemoteRepository {
    suspend fun insertCategory(category: CategoryRemote): Result<Unit, Throwable>
    suspend fun readAllCategory(): Result<Flow<List<CategoryRemote>>, Throwable>
    suspend fun readCategoryWithId(categoryId: Int): Result<CategoryRemote, Throwable>
    suspend fun updateCategory(category: CategoryRemote): Result<Unit, Throwable>
    suspend fun deleteCategory(category: CategoryRemote): Result<Unit, Throwable>
}

internal class CategoryRemoteRepositoryImpl @Inject constructor(
    firebaseAuth: FirebaseAuth,
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) : CategoryRemoteRepository {
    private val dataRef = firebaseAuth.currentUser?.uid?.let {
        FirebaseDatabase.getInstance().getReference(it).child(LIST_CATEGORY)
    }

    override suspend fun insertCategory(category: CategoryRemote): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                dataRef?.child("Category${category.idCategory}")
                    ?.updateChildren(category.toMap())
                    ?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            it.result
                        } else {
                            throwException(it.exception)
                        }
                    }?.await()
                Unit
            }
        }

    override suspend fun readAllCategory(): Result<Flow<List<CategoryRemote>>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                callbackFlow {
                    val valueEventListener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val categoryList = mutableListOf<CategoryRemote>()
                            snapshot.children.forEach {
                                it.getValue(CategoryRemote::class.java)?.let { category ->
                                    categoryList.add(category)
                                }
                            }
                            trySend(categoryList).isSuccess
                        }

                        override fun onCancelled(error: DatabaseError) {
                            try {
                                close(error.toException())
                                throw error.toException()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    dataRef?.addValueEventListener(valueEventListener)
                    awaitClose {
                        dataRef?.removeEventListener(valueEventListener)
                    }
                }
            }
        }

    override suspend fun readCategoryWithId(categoryId: Int): Result<CategoryRemote, Throwable> {
        TODO("Not yet implemented")
    }

    override suspend fun updateCategory(category: CategoryRemote): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                val task = dataRef?.child("Category${category.idCategory}")
                    ?.updateChildren(category.toMap())
                task?.await()
                Unit
            }
                .onFailure { exception ->
                    throw exception
                }
        }

    override suspend fun deleteCategory(category: CategoryRemote): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                val task = dataRef?.child("Category${category.idCategory}")?.removeValue()
                task?.await()
                Unit
            }
                .onFailure { exception ->
                    throw exception
                }
        }

    companion object {
        private const val LIST_CATEGORY = "Categories"
    }
}