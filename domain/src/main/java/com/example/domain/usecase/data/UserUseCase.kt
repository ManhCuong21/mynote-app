package com.example.domain.usecase.data

import com.example.data.dataremote.repository.UserRepository
import com.github.michaelbull.result.Result
import javax.inject.Inject

class UserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun signUpUser(email: String, password: String): Result<Any, Throwable> =
        userRepository.signUpUser(email, password)

    suspend fun signInUser(email: String, password: String): Result<Any, Throwable> =
        userRepository.signInUser(email, password)

    suspend fun signOutUser(): Result<Any, Throwable> =
        userRepository.signOutUser()
}