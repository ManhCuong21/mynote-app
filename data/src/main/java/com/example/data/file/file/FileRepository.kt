package com.example.data.file.file

import java.io.File

interface FileRepository {
    fun createOrGetDirectory(directoryPath: String): File
    suspend fun saveFileToDirectory(directoryName: String)
    suspend fun saveFileToTemp(directoryName: String)
    suspend fun deleteDirectory(directoryName: String)
    suspend fun deleteDirectoryTemp()
}