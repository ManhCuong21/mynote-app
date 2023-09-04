package com.example.data.dataremote.repository

import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.sharepref.SharedPrefersManager
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface UserRepository {
    suspend fun signUpUser(email: String, password: String): Result<Any, Throwable>
    suspend fun signInUser(email: String, password: String): Result<Any, Throwable>
    suspend fun signOutUser(): Result<Any, Throwable>
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
                        try {
                            throw task.exception!!
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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
                        try {
                            throw task.exception!!
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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
}