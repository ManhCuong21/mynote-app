package com.example.data.file.record

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.fragment.app.FragmentActivity
import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.model.ItemRecord
import com.example.data.file.file.FileRepository
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
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
            val fos = FileOutputStream(File(file, "amplitudes.txt"))
            val out = ObjectOutputStream(fos)
            out.writeObject(amplitudes)
            fos.close()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun saveRecordToDirectory(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ) {
        val fileDirectoryTemp = fileRepository.createDirectory(fragmentActivity, "Temp")
        fileDirectoryTemp.listFiles()
            ?.filter { it.canRead() && it.isFile && it.name.endsWith(".mp4") }?.map {
                try {
                    val pathRecord = "${
                        fileRepository.createDirectory(fragmentActivity, directoryName)
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
            val fileDirectoryTemp = fileRepository.createDirectory(fragmentActivity, "Temp")
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
                                    pathRecord = it.path,
                                    amplitudes = convertFileToFloatArray(partAmplitude)
                                )
                            )
                        }
                }
            listRecord
        }
    }

    private fun convertFileToFloatArray(pathFile: String): List<Float> {
        val array: ArrayList<Float> = arrayListOf()
        val reader: BufferedReader?
        try {
            reader = BufferedReader(FileReader(pathFile))
            var input: String?
            while ((reader.readLine().also { input = it }) != null) {
                val number = input!!.trim { it <= ' ' }.split("\\s+".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray().map { it.toFloat() }

                array.addAll(number)
                break
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return array
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