package com.example.domain.usecase.data

import androidx.fragment.app.FragmentActivity
import com.example.data.dataremote.repository.FirebaseStorageRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class FirebaseStorageUseCase @Inject constructor(
    private val firebaseStorageRepository: FirebaseStorageRepository
) {
    suspend fun saveFile(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ): Result<Unit, Throwable> =
        firebaseStorageRepository.uploadDirectory(fragmentActivity, directoryName)
            .map { it.first() }

    suspend fun saveListFileToTemp(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ): Result<Unit, Throwable> =
        firebaseStorageRepository.saveListFileToTemp(fragmentActivity, directoryName)
            .map { it.first() }

    suspend fun deleteAllDirectory(): Result<Unit, Throwable> =
        firebaseStorageRepository.deleteAllDirectory().map { it.first() }

    suspend fun deleteDirectory(directoryName: String): Result<Unit, Throwable> =
        firebaseStorageRepository.deleteDirectory(directoryName).map { it.first() }
}