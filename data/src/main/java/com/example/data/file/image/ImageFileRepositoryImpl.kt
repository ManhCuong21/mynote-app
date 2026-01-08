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
            // Sử dụng đối tượng File để nối đường dẫn an toàn
            val imageFile = File(tempDir, "${System.currentTimeMillis()}.png")

            try {
                FileOutputStream(imageFile).use { outputStream ->
                    // Sử dụng PNG để giữ chất lượng ảnh tốt nhất cho Note
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                }
            } catch (e: Exception) {
                Log.e("ImageFileRepo", "Lỗi lưu ảnh vào Temp: ${e.message}")
            }
        }
    }

    override suspend fun getListImage(): List<ItemImage> {
        return withContext(appCoroutineDispatchers.io) {
            val listImage = arrayListOf<ItemImage>()
            val fileDirectoryTemp = fileRepository.createOrGetDirectory("Temp")

            fileDirectoryTemp.listFiles()
                ?.filter {
                    it.exists() && it.isFile && it.canRead() &&
                            (it.name.endsWith(".jpg", true) || it.name.endsWith(".png", true))
                }
                ?.forEach { file ->
                    try {
                        // TỐI ƯU: Không nên dùng readBytes() vì tốn RAM gấp đôi
                        // Decode trực tiếp từ file path
                        val bmp = BitmapFactory.decodeFile(file.absolutePath)

                        if (bmp != null) {
                            // Nếu bạn chỉ hiển thị lên danh sách, đừng tạo thêm một bản "safeBitmap"
                            // vì nó sẽ ngốn gấp đôi RAM. Chỉ truyền trực tiếp bitmap đã decode.
                            listImage.add(ItemImage(file.absolutePath, bmp))
                        } else {
                            Log.w("readImage", "Không thể decode ảnh: ${file.absolutePath}")
                        }
                    } catch (e: Exception) {
                        Log.e("readImage", "Lỗi đọc ảnh từ file: ${file.absolutePath}", e)
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