package com.example.domain.usecase.data

import androidx.fragment.app.FragmentActivity
import com.example.data.dataremote.repository.FirebaseStorageRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.runCatching
import javax.inject.Inject

class FirebaseStorageUseCase @Inject constructor(
    private val firebaseStorageRepository: FirebaseStorageRepository
) {
    suspend fun saveFile(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ): Result<Unit, Throwable> = runCatching {
        firebaseStorageRepository.uploadDirectory(fragmentActivity, directoryName)
    }

    suspend fun saveListFileToTemp(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ): Result<Unit, Throwable> =
        firebaseStorageRepository.saveListFileToTemp(fragmentActivity, directoryName)
            .map { it.toString() }

    suspend fun deleteAllDirectory(): Result<Unit, Throwable> =
        firebaseStorageRepository.deleteAllDirectory().map { it.toString() }

    suspend fun deleteDirectory(directoryName: String): Result<Unit, Throwable> =
        firebaseStorageRepository.deleteDirectory(directoryName).map { it.toString() }
}