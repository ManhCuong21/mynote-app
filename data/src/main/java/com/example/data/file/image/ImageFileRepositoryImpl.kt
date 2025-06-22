package com.example.data.file.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.fragment.app.FragmentActivity
import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.model.ItemImage
import com.example.data.file.file.FileRepository
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ImageFileRepositoryImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val fileRepository: FileRepository
) : ImageFileRepository {

    override suspend fun saveImageToDirectory(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ) {
        withContext(appCoroutineDispatchers.io) {
            try {
                val tempDir = fileRepository.createOrGetDirectory(fragmentActivity, "Temp")
                val targetDir = fileRepository.createOrGetDirectory(fragmentActivity, directoryName)

                // ❗ Đừng để 2 thư mục trùng nhau!
                if (tempDir.absolutePath == targetDir.absolutePath) {
                    throw IllegalStateException("❌ Temp và Directory không được giống nhau!")
                }

                // ✅ Xoá ảnh cũ trong thư mục đích (nhưng giữ thư mục)
                targetDir.listFiles()?.forEach { file ->
                    if (file.isFile &&
                        (file.name.endsWith(".jpg", true) || file.name.endsWith(".png", true))
                    ) {
                        file.delete()
                    }
                }

                // ✅ Copy từng ảnh từ Temp sang Directory
                val imageFiles = tempDir.listFiles()
                    ?.filter { it.canRead() && it.isFile && (it.name.endsWith(".jpg", true) || it.name.endsWith(".png", true)) }
                    ?: return@withContext

                imageFiles.forEachIndexed { index, file ->
                    val bytes = file.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    val outputFile = File(targetDir, file.nameWithoutExtension + ".png")
                    outputFile.parentFile?.mkdirs()

                    FileOutputStream(outputFile).use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }

                    println("✅ [$index] Lưu vào thư mục chính: ${outputFile.name}")
                }
            } catch (e: Exception) {
                println("❌ Lỗi khi lưu ảnh từ Temp vào $directoryName: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override suspend fun saveImageFromDirectoryToTemp(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ) {
        withContext(appCoroutineDispatchers.io) {
            try {
                val sourceDir = fileRepository.createOrGetDirectory(fragmentActivity, directoryName)
                val tempDir = fileRepository.createOrGetDirectory(fragmentActivity, "Temp")

                // ⚠️ Không cho phép source và temp trùng nhau
                if (sourceDir.absolutePath == tempDir.absolutePath) {
                    throw IllegalStateException("❌ Thư mục nguồn và Temp không được giống nhau.")
                }

                // 🔍 Lọc file ảnh hợp lệ
                val imageFiles = sourceDir.listFiles()
                    ?.filter {
                        it.canRead() && it.isFile &&
                                (it.name.endsWith(".jpg", ignoreCase = true) || it.name.endsWith(".png", ignoreCase = true))
                    }
                    ?: run {
                        println("⚠️ Không tìm thấy ảnh nào trong thư mục: ${sourceDir.absolutePath}")
                        return@withContext
                    }

                println("📁 Copy ${imageFiles.size} ảnh từ ${sourceDir.name} sang Temp...")

                imageFiles.forEachIndexed { index, file ->
                    try {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)

                        if (bitmap == null) {
                            println("⚠️ [$index] Không thể decode ảnh: ${file.name}")
                            return@forEachIndexed
                        }

                        val outputFile = File(tempDir, file.nameWithoutExtension + ".png")

                        // ✅ Đảm bảo thư mục tồn tại
                        outputFile.parentFile?.mkdirs()

                        FileOutputStream(outputFile).use { stream ->
                            val success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                            if (!success) {
                                throw RuntimeException("❌ Compress thất bại: ${file.name}")
                            }
                        }

                        println("✅ [$index] Đã lưu ảnh: ${outputFile.name}")
                    } catch (e: Exception) {
                        println("❌ [$index] Lỗi khi lưu ảnh: ${file.name} - ${e.message}")
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                println("❌ Lỗi khi sao chép ảnh: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override suspend fun saveImageToTemp(fragmentActivity: FragmentActivity, bitmap: Bitmap) {
        withContext(appCoroutineDispatchers.io) {
            val fileName = "${
                fileRepository.createOrGetDirectory(fragmentActivity, "Temp")
            }/${System.currentTimeMillis()}.jpg"
            try {
                val outputStream = FileOutputStream(fileName)
                outputStream.flush()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun readImage(fragmentActivity: FragmentActivity): List<ItemImage> {
        return withContext(appCoroutineDispatchers.io) {
            val listImage = arrayListOf<ItemImage>()
            val fileDirectoryTemp = fileRepository.createOrGetDirectory(fragmentActivity, "Temp")
            fileDirectoryTemp.listFiles()
                ?.filter {
                    it.canRead() && it.isFile && (it.name.endsWith(
                        ".jpg",
                        true
                    ) || it.name.endsWith(".png", true))
                }
                ?.map {
                    if (it.exists()) {
                        val bytes = it.readBytes()
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        listImage.add(ItemImage(it.absolutePath, Bitmap.createBitmap(bmp)))
                    }
                }
            listImage
        }
    }

    override suspend fun deleteImage(imagePath: String) {
        withContext(appCoroutineDispatchers.io) {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}