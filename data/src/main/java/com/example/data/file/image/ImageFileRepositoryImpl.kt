package com.example.data.file.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
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

    override suspend fun saveImageToTemp(bitmap: Bitmap) {
        withContext(appCoroutineDispatchers.io) {
            val tempDir = fileRepository.createOrGetDirectory("Temp")
            // Sử dụng PNG cho chất lượng, nhưng có thể cân nhắc WEBP để giảm dung lượng khi Sync
            val imageFile = File(tempDir, "IMG_${System.currentTimeMillis()}.png")

            try {
                FileOutputStream(imageFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
            } catch (e: Exception) {
                Log.e("ImageFileRepo", "Lỗi lưu ảnh: ${e.message}")
            }
        }
    }

    override suspend fun getListImage(): List<ItemImage> = withContext(appCoroutineDispatchers.io) {
        val tempDir = fileRepository.createOrGetDirectory("Temp")

        tempDir.listFiles()
            ?.filter { it.isFile && it.canRead() && it.isImageFile() }
            ?.mapNotNull { file ->
                try {
                    // TỐI ƯU: Sử dụng inSampleSize nếu ảnh quá lớn để tránh crash RAM
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = false
                        inPreferredConfig = Bitmap.Config.RGB_565 // Tiết kiệm 50% RAM so với ARGB_8888
                    }
                    val bmp = BitmapFactory.decodeFile(file.absolutePath, options)
                    bmp?.let { ItemImage(file.absolutePath, it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            } ?: emptyList()
    }

    private fun File.isImageFile() = name.lowercase().let {
        it.endsWith(".jpg") || it.endsWith(".png") || it.endsWith(".jpeg")
    }

    override suspend fun deleteImage(imagePath: String) {
        withContext(appCoroutineDispatchers.io) {
            File(imagePath).let { if (it.exists()) it.delete() }
        }
    }
}