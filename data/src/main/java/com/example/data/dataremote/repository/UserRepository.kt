package com.example.data.dataremote.repository

import android.util.Log
import com.example.core.core.external.AppCoroutineDispatchers
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface UserRepository {
    suspend fun signUpUser(email: String, password: String): Result<Any, Throwable>
    suspend fun signInUser(email: String, password: String): Result<AuthResult, Throwable>
}

internal class UserRepositoryImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val firebaseAuth: FirebaseAuth
) : UserRepository {
    override suspend fun signUpUser(
        email: String,
        password: String
    ): Result<Any, Throwable> = withContext(appCoroutineDispatchers.io) {
        runCatching {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        Log.e("TAG", "signUpUser success: $user")
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
    ): Result<AuthResult, Throwable> = withContext(appCoroutineDispatchers.io) {
        runCatching {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = firebaseAuth.currentUser
                        Log.e("TAG", "signInWithEmail:$user")
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e("TAG", "signInWithEmail:failure", task.exception)
                    }
                }.await()
        }
    }
}