package com.example.data.file.record

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.fragment.app.FragmentActivity
import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.model.ItemRecord
import com.example.data.file.FileRepository
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class RecordFileRepositoryImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val fileRepository: FileRepository
) : RecordFileRepository {
    override fun saveRecordToDirectory(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ) {
        val fileDirectoryTemp = fileRepository.getOutputMediaDirectoryTemp(fragmentActivity)
        fileDirectoryTemp.listFiles()
            ?.filter { it.canRead() && it.isFile && it.name.endsWith(".mp4") }?.map {
                try {
                    val pathRecord = "${
                        fileRepository.getOutputMediaDirectory(fragmentActivity, directoryName)
                    }/${it.name}"
                    val bytes = it.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val outputStream = FileOutputStream(pathRecord)
                    outputStream.flush()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    override suspend fun readRecord(fragmentActivity: FragmentActivity): List<ItemRecord> {
        return withContext(appCoroutineDispatchers.io) {
            val listRecord = arrayListOf<ItemRecord>()
            val fileDirectoryTemp = fileRepository.getOutputMediaDirectoryTemp(fragmentActivity)
            fileDirectoryTemp.listFiles()
                ?.filter { it.canRead() && it.isFile && it.name.endsWith(".mp4") }?.map {
                    listRecord.add(ItemRecord(pathRecord = it.path))
                }
            listRecord
        }
    }

    override suspend fun deleteRecord(pathRecord: String) {
        withContext(appCoroutineDispatchers.io) {
            val file = File(pathRecord)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}