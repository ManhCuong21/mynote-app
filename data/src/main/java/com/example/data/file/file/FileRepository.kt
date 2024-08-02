package com.example.data.file.file

import androidx.fragment.app.FragmentActivity
import com.github.michaelbull.result.Result
import java.io.File

interface FileRepository {
    fun createOrGetDirectory(fragmentActivity: FragmentActivity, directoryPath: String): File
    fun saveFileToDirectory(fragmentActivity: FragmentActivity, directoryName: String)
    fun saveFileToTemp(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ): Result<Unit, Throwable>

    suspend fun deleteDirectory(fragmentActivity: FragmentActivity, directoryName: String)
    suspend fun deleteDirectoryTemp(fragmentActivity: FragmentActivity)
}