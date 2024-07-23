package com.example.domain.usecase.file

import androidx.fragment.app.FragmentActivity
import com.example.data.file.FileRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import java.io.File
import javax.inject.Inject

class FileUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    fun getOutputMediaDirectory(fragmentActivity: FragmentActivity, pathDirectory: String): File =
        fileRepository.createDirectory(fragmentActivity, pathDirectory)

    fun getOutputMediaDirectoryTemp(fragmentActivity: FragmentActivity): File =
        fileRepository.createDirectoryTemp(fragmentActivity)

    fun saveFileToDirectory(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ): Result<Unit, Throwable> =
        runCatching {
            fileRepository.saveFileToDirectory(fragmentActivity, directoryName)
        }

    fun saveFileToTemp(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ): Result<Unit, Throwable> =
        fileRepository.saveFileToTemp(fragmentActivity, directoryName)

    suspend fun deleteDirectory(fragmentActivity: FragmentActivity, directoryName: String) =
        fileRepository.deleteDirectory(fragmentActivity, directoryName)

    suspend fun deleteDirectoryTemp(fragmentActivity: FragmentActivity) =
        fileRepository.deleteDirectoryTemp(fragmentActivity)
}