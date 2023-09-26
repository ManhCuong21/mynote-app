package com.example.data.dataremote.repository

import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.external.throwException
import com.example.data.dataremote.model.NoteRemote
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface NoteRemoteRepository {
    suspend fun insertNote(note: NoteRemote): Result<Unit, Throwable>
    suspend fun readAllNote(): Result<Flow<List<NoteRemote>>, Throwable>
    suspend fun readNoteWithCategory(idCategory: Long): Result<Flow<List<NoteRemote>>, Throwable>
    suspend fun updateNote(note: NoteRemote): Result<Unit, Throwable>
    suspend fun deleteNote(note: NoteRemote): Result<Unit, Throwable>
}

class NoteRemoteRepositoryImpl @Inject constructor(
    firebaseAuth: FirebaseAuth,
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) : NoteRemoteRepository {
    private val dataRef = firebaseAuth.currentUser?.uid?.let {
        FirebaseDatabase.getInstance().getReference(it)
            .child(LIST_NOTE)
    }

    override suspend fun insertNote(note: NoteRemote): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                dataRef?.child("Note${note.idNote}")
                    ?.updateChildren(note.toMap())
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result
                        } else {
                            throwException(task.exception)
                        }
                    }?.await()
                Unit
            }
        }

    override suspend fun readAllNote(): Result<Flow<List<NoteRemote>>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                callbackFlow {
                    val valueEventListener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val listNote = mutableListOf<NoteRemote>()
                            snapshot.children.forEach {
                                it.getValue(NoteRemote::class.java)?.let { note ->
                                    listNote.add(note)
                                }
                            }
                            trySend(listNote).isSuccess
                        }

                        override fun onCancelled(error: DatabaseError) {
                            try {
                                close(error.toException())
                                throw error.toException()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    dataRef?.addValueEventListener(valueEventListener)
                    awaitClose {
                        dataRef?.removeEventListener(valueEventListener)
                    }
                }
            }
        }

    override suspend fun readNoteWithCategory(idCategory: Long): Result<Flow<List<NoteRemote>>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                callbackFlow {
                    val valueEventListener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val listNote = mutableListOf<NoteRemote>()
                            snapshot.children.forEach {
                                it.getValue(NoteRemote::class.java)?.let { note ->
                                    if (note.categoryRemote?.idCategory == idCategory) {
                                        listNote.add(note)
                                    }
                                }
                            }
                            trySend(listNote).isSuccess
                        }

                        override fun onCancelled(error: DatabaseError) {
                            try {
                                close(error.toException())
                                throw error.toException()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    dataRef?.addValueEventListener(valueEventListener)
                    awaitClose {
                        dataRef?.removeEventListener(valueEventListener)
                    }
                }
            }
        }

    override suspend fun updateNote(note: NoteRemote) =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                dataRef?.child("Note${note.idNote}")
                    ?.updateChildren(note.toMap())
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result
                        } else {
                            throwException(task.exception)
                        }
                    }?.await()
                Unit
            }
        }

    override suspend fun deleteNote(note: NoteRemote) =
        withContext(appCoroutineDispatchers.io) {
            runCatching {
                dataRef?.child("Note${note.idNote}")?.removeValue()?.await()
                Unit
            }
        }

    companion object {
        private const val LIST_NOTE = "Notes"
    }
}