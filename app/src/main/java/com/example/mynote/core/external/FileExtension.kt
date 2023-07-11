package com.example.mynote.core.external

import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.BaseColumns
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.example.mynote.core.external.AppConstants.Companion.FILE_NAME_FORMAT
import com.example.mynote.domain.model.ItemImage
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


interface FileExtension {
    fun getOutputMediaDirectory(fragmentActivity: FragmentActivity, pathDirectory: String): File
    fun saveImageToFile(fragmentActivity: FragmentActivity, pathChild: Uri, bitmap: Bitmap)
    suspend fun readImageFromFile(
        fragmentActivity: FragmentActivity,
        pathChild: String
    ): List<ItemImage>

    suspend fun deleteImageFromFile(
        fragmentActivity: FragmentActivity,
        intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>,
        pathImage: String
    )
}

class FileExtensionImpl @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) : FileExtension {

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

    override fun saveImageToFile(
        fragmentActivity: FragmentActivity,
        pathChild: Uri,
        bitmap: Bitmap
    ) {
        val pathFile = SimpleDateFormat(
            FILE_NAME_FORMAT,
            Locale.getDefault()
        ).format(System.currentTimeMillis())
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_INTERNAL)
        } else MediaStore.Images.Media.INTERNAL_CONTENT_URI
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$pathFile.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
        }
        val contentResolver = fragmentActivity.contentResolver
        try {
            contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                contentResolver.openOutputStream(uri).use { outputStream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                        throw IOException("Couldn't save bitmap")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun readImageFromFile(
        fragmentActivity: FragmentActivity,
        pathChild: String
    ): List<ItemImage> {
        return withContext(appCoroutineDispatchers.io) {
            val files = getOutputMediaDirectory(fragmentActivity, pathChild).listFiles()
            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
                val bytes = it.readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ItemImage(it.absolutePath, image = Bitmap.createBitmap(bmp))
            } ?: listOf()
        }
    }

    override suspend fun deleteImageFromFile(
        fragmentActivity: FragmentActivity,
        intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>,
        pathImage: String
    ) {
        return withContext(appCoroutineDispatchers.io) {
            val contentResolver = fragmentActivity.contentResolver
            val uri =
                getContentUriId(imageUri = pathImage.toUri(), contentResolver = contentResolver)
            try {
                // android 28 and below
                contentResolver.delete(uri, null, null)
            } catch (e: SecurityException) {
                val intentSender = when {
                    // android 30 (Android 11)
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        MediaStore.createDeleteRequest(
                            fragmentActivity.contentResolver,
                            listOf(uri)
                        ).intentSender
                    }
                    // android 29 (Android 10)
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        val recoverableSecurityException = e as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }

                    else -> null
                }
                intentSender?.let { sender ->
                    intentSenderLauncher.launch(
                        IntentSenderRequest.Builder(sender).build()
                    )
                }
            }
        }
    }

    private fun getContentUriId(imageUri: Uri, contentResolver: ContentResolver): Uri {
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = MediaStore.Images.Media.DATA + " = ?"
        val selectionArgs = arrayOf(imageUri.path)
        var id: Long = 0
        val cursor: Cursor? =
            contentResolver.query(queryUri, projection, selection, selectionArgs, null)
        if (cursor != null) {
            if (cursor.count > 0) {
                cursor.moveToFirst()
                id = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID))
            }
        }
        cursor?.close()
        return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
    }
}