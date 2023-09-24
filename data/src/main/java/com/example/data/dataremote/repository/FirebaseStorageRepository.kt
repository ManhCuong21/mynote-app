package com.example.data.dataremote.repository

import androidx.fragment.app.FragmentActivity
import com.example.core.core.external.AppCoroutineDispatchers
import com.example.data.file.FileRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.runCatching
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

interface FirebaseStorageRepository {
    suspend fun uploadDirectory(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ): Result<Flow<Unit>, Throwable>

    suspend fun uploadFile(directoryName: String, pathFile: String): Result<Unit, Throwable>

    suspend fun getListFile(fragmentActivity: FragmentActivity, directoryName: String):
            Result<Flow<List<String>>, Throwable>

    suspend fun saveListFileToTemp(fragmentActivity: FragmentActivity, directoryName: String):
            Result<Flow<Unit>, Throwable>

    suspend fun deleteDirectory(directoryName: String): Result<Unit, Throwable>
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
        directoryName: String
    ): Result<Flow<Unit>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                callbackFlow {
                    val directory = fileRepository.getOutputMediaDirectoryTemp(fragmentActivity)
                    val listFile = directory.listFiles()
                    if (directory.isDirectory && !listFile.isNullOrEmpty()) {
                        listFile.forEachIndexed { index, itemImage ->
                            uploadFile(directoryName, itemImage.path).onSuccess {
                                if (index == listFile.size - 1) {
                                    trySend(Unit)
                                }
                            }
                        }
                    } else {
                        trySend(Unit)
                    }
                    awaitClose { }
                }
            }
        }

    override suspend fun uploadFile(
        directoryName: String,
        pathFile: String
    ): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                val stream = FileInputStream(File(pathFile))
                val imageRef =
                    storageRef?.child(directoryName)?.child(pathFile.substringAfterLast("/"))
                val uploadTask = imageRef?.putStream(stream)
                uploadTask?.addOnSuccessListener {
                }?.addOnFailureListener { exception ->
                    try {
                        throw exception
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                Unit
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
                }?.addOnFailureListener {
                    try {
                        throw it
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }?.await()
            awaitClose { }
        }
    }

    override suspend fun saveListFileToTemp(
        fragmentActivity: FragmentActivity,
        directoryName: String
    ): Result<Flow<Unit>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                callbackFlow {
                    val directoryTemp = fileRepository.getOutputMediaDirectoryTemp(fragmentActivity)
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
                                .addOnFailureListener {
                                    try {
                                        throw it
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                        }
                    }
                    awaitClose { }
                }
            }
        }

    override suspend fun deleteDirectory(directoryName: String): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                storageRef?.child(directoryName)?.listAll()
                    ?.addOnSuccessListener { result ->
                        result.items.forEach {
                            firebaseStorage.reference.child(it.path).delete()
                                .addOnFailureListener { exception ->
                                    try {
                                        throw exception
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                        }
                    }?.addOnFailureListener {
                        try {
                            throw it
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }?.await()
                Unit
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
                        try {
                            throw exception
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }?.await()
                Unit
            }
        }
}