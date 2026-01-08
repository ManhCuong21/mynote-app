package com.example.domain.usecase.file

import com.example.data.file.file.FileRepository
import java.io.File
import javax.inject.Inject

class FileUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    fun createDirectoryTemp(): File =
        fileRepository.createOrGetDirectory("Temp")

    suspend fun saveFileToDirectory(directoryName: String) =
        fileRepository.saveFileToDirectory(directoryName)

    suspend fun saveFileToTemp(directoryName: String) =
        fileRepository.saveFileToTemp(directoryName)

    suspend fun deleteDirectory(directoryName: String) =
        fileRepository.deleteDirectory(directoryName)

    suspend fun deleteDirectoryTemp() =
        fileRepository.deleteDirectoryTemp()
}