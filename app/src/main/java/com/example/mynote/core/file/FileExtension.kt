package com.example.mynote.core.file

import androidx.fragment.app.FragmentActivity
import java.io.File
import javax.inject.Inject

interface FileExtension {
    fun getOutputMediaDirectory(fragmentActivity: FragmentActivity, pathDirectory: String): File

}

class FileExtensionImpl @Inject constructor() : FileExtension {
    override fun getOutputMediaDirectory(
        fragmentActivity: FragmentActivity,
        pathDirectory: String
    ): File {
        val mediaDir = fragmentActivity.externalMediaDirs.firstOrNull()?.let { file ->
            File(file, pathDirectory).apply {
                mkdir()
            }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else fragmentActivity.filesDir
    }
}