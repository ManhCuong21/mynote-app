package com.example.data.file.record

import androidx.fragment.app.FragmentActivity
import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.model.ItemRecord
import com.example.data.file.file.FileRepository
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import javax.inject.Inject

class RecordFileRepositoryImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val fileRepository: FileRepository
) : RecordFileRepository {
    override fun saveAmplitude(
        file: File,
        amplitudes: List<Float>
    ) {
        try {
            val fileOutputStream = FileOutputStream(File(file, "amplitudes.txt"))
            val objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(amplitudes)
            objectOutputStream.close()
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun saveRecordToDirectory(
        fragmentActivity: FragmentActivity,
        pathDirectory: String
    ) {
        val fileDirectoryTempRecord = fileRepository.createOrGetDirectory(fragmentActivity, "Temp")
        fileDirectoryTempRecord.listFiles()
            ?.filter { it.canRead() && it.isDirectory && it.name.startsWith("Record") }
            ?.map { recordDirectory ->
                recordDirectory.listFiles()?.map {
                    try {
                        val externalPath = "${
                            fileRepository.createOrGetDirectory(
                                fragmentActivity,
                                "$pathDirectory/${recordDirectory.name}"
                            )
                        }/${it.name}"
                        val externalFile = File(externalPath)
                        val fileInputStream = FileInputStream(it)
                        val fileOutputStream = FileOutputStream(externalFile)

                        val buffer = ByteArray(1024)
                        var length: Int
                        while ((fileInputStream.read(buffer)
                                .also { lengthBuffer -> length = lengthBuffer }) > 0
                        ) {
                            fileOutputStream.write(buffer, 0, length)
                        }

                        fileInputStream.close()
                        fileOutputStream.flush()
                        fileOutputStream.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }

    override suspend fun readRecord(fragmentActivity: FragmentActivity): List<ItemRecord> {
        return withContext(appCoroutineDispatchers.io) {
            val listRecord = arrayListOf<ItemRecord>()
            val fileDirectoryTemp = fileRepository.createOrGetDirectory(fragmentActivity, "Temp")
            fileDirectoryTemp.listFiles()
                ?.filter { directory ->
                    directory.canRead() && directory.isDirectory && directory.name.startsWith("Record")
                }?.map { file ->
                    val partAmplitude = file.listFiles()
                        ?.filter { fileAmplitude ->
                            fileAmplitude.canRead() && fileAmplitude.isFile &&
                                    fileAmplitude.name.startsWith("amplitude")
                        }
                        ?.map { fileAmp -> fileAmp.path }?.get(0) ?: ""

                    file.listFiles()
                        ?.filter { it.canRead() && it.isFile && it.name.endsWith(".mp4") }?.map {
                            listRecord.add(
                                ItemRecord(
                                    pathDirectory = file.path,
                                    pathRecord = it.path,
                                    amplitudes = convertFileToFloatArray(partAmplitude)
                                )
                            )
                        }
                }
            listRecord
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun convertFileToFloatArray(filePath: String): List<Float> {
        return withContext(appCoroutineDispatchers.io) {
            val amplitudes = ArrayList<Float>()
            try {
                try {
                    val fileInputStream = FileInputStream(filePath)
                    val objectInputStream = ObjectInputStream(fileInputStream)
                    amplitudes.addAll(objectInputStream.readObject() as ArrayList<Float>)
                    objectInputStream.close()
                    fileInputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            amplitudes
        }
    }

    override suspend fun deleteRecord(pathRecord: String) {
        withContext(appCoroutineDispatchers.io) {
            val file = File(pathRecord)
            if (file.isDirectory) {
                file.listFiles()?.forEach {
                    if (it.exists()) {
                        it.delete()
                    }
                }
            }
            file.delete()
        }
    }
}