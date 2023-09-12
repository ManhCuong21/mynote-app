package com.example.domain.usecase.file

import androidx.fragment.app.FragmentActivity
import com.example.data.file.FileRepository
import java.io.File
import javax.inject.Inject

class FileUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    fun getOutputMediaDirectory(fragmentActivity: FragmentActivity, pathDirectory: String): File =
        fileRepository.getOutputMediaDirectory(fragmentActivity, pathDirectory)

    fun getOutputMediaDirectoryTemp(fragmentActivity: FragmentActivity): File =
        fileRepository.getOutputMediaDirectoryTemp(fragmentActivity)

    fun saveFileToDirectory(fragmentActivity: FragmentActivity, directoryName: String) =
        fileRepository.saveFileToDirectory(fragmentActivity, directoryName)

    fun saveFileToTemp(fragmentActivity: FragmentActivity, directoryName: String) =
        fileRepository.saveFileToTemp(fragmentActivity, directoryName)

    suspend fun deleteDirectory(fragmentActivity: FragmentActivity, directoryName: String) =
        fileRepository.deleteDirectory(fragmentActivity, directoryName)

    suspend fun deleteDirectoryTemp(fragmentActivity: FragmentActivity) =
        fileRepository.deleteDirectoryTemp(fragmentActivity)
}