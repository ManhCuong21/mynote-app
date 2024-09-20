package com.example.data.dataremote.repository

import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.external.throwException
import com.example.data.file.file.FileRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject


interface FirebaseStorageRepository {
    suspend fun uploadDirectory(
        fragmentActivity: FragmentActivity,
        directoryRoot: String
    ): Flow<Unit>

    suspend fun uploadFileImage(directoryName: String, pathFile: String): Flow<Unit>
    suspend fun uploadFileRecord(directoryRoot: String, directoryName: String, pathFile: String)
    suspend fun getListFile(fragmentActivity: FragmentActivity, directoryName: String):
            Result<Flow<List<String>>, Throwable>

    suspend fun saveListFileToTemp(fragmentActivity: FragmentActivity, directoryName: String):
            Result<Flow<Unit>, Throwable>

    suspend fun deleteAllDirectory(): Result<Flow<Unit>, Throwable>
    suspend fun deleteDirectory(directoryName: String): Result<Flow<Unit>, Throwable>
    suspend fun deleteFile(directoryName: String, fileName: String): Result<Unit, Throwable>
}

internal class FirebaseStorageRepositoryImpl @Inject constructor(
    firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val fileRepository: FileRepository
) : FirebaseStorageRepository {
    private val storageRef = firebaseAuth.currentUser?.uid?.let {
        firebaseStorage.reference.child(it)
    }

    override suspend fun uploadDirectory(
        fragmentActivity: FragmentActivity,
        directoryRoot: String
    ): Flow<Unit> = flow {
        withContext(appCoroutineDispatchers.io) {
            val directory = fileRepository.createOrGetDirectory(fragmentActivity, "Temp")
            // Get all files and subfolders in the current folder
            val files = directory.listFiles() ?: return@withContext

            for (file in files) {
                val path = if (directoryRoot.isEmpty()) {
                    file.name
                } else {
                    "$directoryRoot/${file.name}"
                }

                if (file.isDirectory) {
                    // Recursively upload the subfolder
                    emitAll(uploadDirectory(fragmentActivity, path))
                } else {
                    // Upload the file
                    val uri = Uri.fromFile(file)
                    val uploadTask = storageRef?.child(path)?.putFile(uri)

                    uploadTask?.addOnSuccessListener {
                        CoroutineScope(appCoroutineDispatchers.io).launch {
                            emit(Unit)
                        }
                        // File upload successful
                    }?.addOnFailureListener { exception ->
                        throw exception
                    }?.await()
                }
            }
        }
    }
//    {
//        val directory = fileRepository.createOrGetDirectory(fragmentActivity, "Temp")
//        val listFile = directory.listFiles()
//        if (directory.isDirectory && !listFile.isNullOrEmpty()) {
//            listFile.forEach { item ->
//                if (item.isDirectory && !item.listFiles().isNullOrEmpty()) {
//                    item.listFiles()?.forEach {
//                        uploadFileRecord(directoryRoot, item.name, it.path)
//                    }
//                } else {
//                    uploadFileImage(directoryRoot, item.path)
//                }
//            }
//        }
//    }

    override suspend fun uploadFileImage(
        directoryName: String,
        pathFile: String
    ): Flow<Unit> = flow {
        withContext(appCoroutineDispatchers.io) {
            val stream = FileInputStream(File(pathFile))
            val imageRef =
                storageRef?.child(directoryName)?.child(pathFile.substringAfterLast("/"))
            val uploadTask = imageRef?.putStream(stream)
            uploadTask?.addOnSuccessListener {
            }?.addOnFailureListener { exception ->
                throwException(exception)
            }?.await()
        }
    }

    override suspend fun uploadFileRecord(
        directoryRoot: String,
        directoryName: String,
        pathFile: String
    ) {
        withContext(appCoroutineDispatchers.io) {
            val stream = FileInputStream(File(pathFile))
            val imageRef =
                storageRef?.child(directoryRoot)?.child(directoryName)
                    ?.child(pathFile.substringAfterLast("/"))
            val uploadTask = imageRef?.putStream(stream)
            uploadTask?.addOnSuccessListener {
            }?.addOnFailureListener { exception ->
                throwException(exception)
            }?.await()
        }
    }

    override suspend fun getListFile(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ): Result<Flow<List<String>>, Throwable> = runCatching {
        callbackFlow {
            storageRef?.child(directoryName)?.listAll()
                ?.addOnSuccessListener { result ->
                    result.items.forEach { it.getBytes(Long.MAX_VALUE) }
                    val listPath = result.items.map { it.path }
                    trySend(listPath)
                }?.addOnFailureListener { exception ->
                    throwException(exception)
                }?.await()
        }
    }

    override suspend fun saveListFileToTemp(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ): Result<Flow<Unit>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                callbackFlow {
                    val directoryTemp =
                        fileRepository.createOrGetDirectory(fragmentActivity, "Temp")
                    val listFile =
                        storageRef?.child(directoryName)?.listAll()?.await()?.items ?: listOf()
                    if (listFile.isEmpty()) {
                        trySend(Unit)
                    } else {
                        listFile.forEach { item ->
                            val file =
                                async { File(directoryTemp, item.path.substringAfterLast("/")) }
                            firebaseStorage.reference.child(item.path).getFile(file.await())
                                .addOnSuccessListener {
                                    if (listFile.indexOf(it.storage) == listFile.size - 1) {
                                        launch {
                                            delay(1000L)
                                            trySend(Unit)
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    throwException(exception)
                                }
                        }
                    }
                    awaitClose { }
                }
            }
        }

    override suspend fun deleteAllDirectory(): Result<Flow<Unit>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                callbackFlow {
                    storageRef?.listAll()?.addOnSuccessListener { directoryRoot ->
                        if (directoryRoot.prefixes.isNotEmpty()) {
                            directoryRoot.prefixes.forEachIndexed { _, directory ->
                                val directoryName = directory.path.substringAfterLast("/")
                                storageRef.child(directoryName).listAll()
                                    .addOnSuccessListener { result ->
                                        if (result.items.isNotEmpty()) {
                                            result.items.forEachIndexed { index, item ->
                                                firebaseStorage.reference.child(item.path)
                                                    .delete()
                                                    .addOnCompleteListener {
                                                        if (index == result.items.size - 1) {
                                                            trySend(Unit)
                                                        }
                                                    }
                                                    .addOnFailureListener { exception ->
                                                        throwException(exception)
                                                    }
                                            }
                                        } else trySend(Unit)
                                    }.addOnFailureListener { exception ->
                                        throwException(exception)
                                    }
                            }
                        } else trySend(Unit)
                    }
                    awaitClose {}
                }
            }
        }

    override suspend fun deleteDirectory(directoryName: String): Result<Flow<Unit>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                callbackFlow {
                    val folderRef = firebaseStorage.reference.child(directoryName)
                    // List all items in the folder
                    folderRef.listAll().addOnSuccessListener { listResult ->
                        // Delete all files in the folder
                        for (fileRef in listResult.items) {
                            fileRef.delete().addOnSuccessListener {
                                println("File deleted: " + fileRef.name)
                            }.addOnFailureListener { exception ->
                                System.err.println("Error deleting file: " + exception.message)
                            }
                        }

                        // Recursively delete subfolders
                        for (prefixRef in listResult.prefixes) {
                            CoroutineScope(appCoroutineDispatchers.io).launch {
                                deleteDirectory(prefixRef.path)
                            }
                        }
                    }.addOnFailureListener { exception ->
                        System.err.println("Error listing files: " + exception.message)
                    }
                }
            }
        }

    override suspend fun deleteFile(
        directoryName: String,
        fileName: String
    ): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                val imageRef = storageRef?.child(directoryName)?.child(fileName)
                imageRef?.delete()
                    ?.addOnSuccessListener {}
                    ?.addOnFailureListener { exception ->
                        throwException(exception)
                    }?.await()
                Unit
            }
        }
}