package com.example.data.dataremote.repository

import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.external.throwException
import com.example.core.core.sharepref.SharedPrefersManager
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface UserRepository {
    suspend fun signUpUser(email: String, password: String): Result<Any, Throwable>
    suspend fun signInUser(email: String, password: String): Result<Any, Throwable>
    suspend fun signOutUser(): Result<Any, Throwable>
    suspend fun deleteUser(): Result<Any, Throwable>
}

internal class UserRepositoryImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val firebaseAuth: FirebaseAuth,
    private val sharedPrefersManager: SharedPrefersManager
) : UserRepository {
    override suspend fun signUpUser(
        email: String,
        password: String
    ): Result<Any, Throwable> = withContext(appCoroutineDispatchers.io) {
        runCatching {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseAuth.currentUser
                    } else {
                        throwException(task.exception)
                    }
                }.await()
        }
    }

    override suspend fun signInUser(
        email: String,
        password: String
    ): Result<Any, Throwable> = withContext(appCoroutineDispatchers.io) {
        runCatching {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        sharedPrefersManager.userEmail = task.result.user?.email
                    } else {
                        throwException(task.exception)
                    }
                }.await()
        }
    }

    override suspend fun signOutUser(): Result<Any, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                firebaseAuth.signOut()
            }
        }

    override suspend fun deleteUser(): Result<Any, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                val currentUser = firebaseAuth.currentUser
                // Delete database
                currentUser?.uid?.let {
                    FirebaseDatabase.getInstance().getReference(it)
                }?.removeValue()?.await()
                // Delete user
                currentUser?.delete()?.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        throwException(task.exception)
                    }
                }?.await() ?: Unit
            }
        }
}