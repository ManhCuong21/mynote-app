package com.example.domain.usecase.file

import androidx.fragment.app.FragmentActivity
import com.example.core.core.external.AppConstants.FILE_NAME_FORMAT
import com.example.core.core.external.formatDate
import com.example.data.file.file.FileRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import java.io.File
import javax.inject.Inject

class FileUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    private fun createDirectory(fragmentActivity: FragmentActivity, pathDirectory: String): File =
        fileRepository.createOrGetDirectory(fragmentActivity, pathDirectory)

    fun createDirectoryTemp(fragmentActivity: FragmentActivity): File =
        createDirectory(fragmentActivity,"Temp")

    fun createDirectoryRecordTemp(fragmentActivity: FragmentActivity): File =
        createDirectory(
            fragmentActivity,
            pathDirectory = "Temp/Record-${formatDate(FILE_NAME_FORMAT)}"
        )

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