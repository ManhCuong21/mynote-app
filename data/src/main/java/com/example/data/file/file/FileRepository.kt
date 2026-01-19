package com.example.data.file.file

import java.io.File
import java.io.InputStream

interface FileRepository {
    fun createOrGetDirectory(directoryPath: String): File
    suspend fun saveFileToDirectory(directoryName: String)
    suspend fun saveFileToTemp(directoryName: String)
    suspend fun deleteDirectory(directoryName: String)
    suspend fun deleteDirectoryTemp()
    suspend fun zipPicturesDirectory(zipFile: File)
    suspend fun unzipFromStream(inputStream: InputStream, targetFolder: File)
}