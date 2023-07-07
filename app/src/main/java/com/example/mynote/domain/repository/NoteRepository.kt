package com.example.mynote.domain.repository

import com.example.mynote.core.external.AppCoroutineDispatchers
import com.example.mynote.data.model.NoteEntity
import com.example.mynote.data.database.NoteDatabase
import com.github.michaelbull.result.Result
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface NoteRepository {
    suspend fun insertNote(note: NoteEntity): Result<Unit, Throwable>
    suspend fun readNote(): Result<List<NoteEntity>, Throwable>
    suspend fun updateNote(note: NoteEntity): Result<Unit, Throwable>
    suspend fun deleteNote(note: NoteEntity): Result<Unit, Throwable>
}

class NoteRepositoryImpl @Inject constructor(
    private val noteDatabase: NoteDatabase,
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) : NoteRepository {
    override suspend fun insertNote(note: NoteEntity): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            noteDatabase.insertNote(note)
        }

    override suspend fun readNote(): Result<List<NoteEntity>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            noteDatabase.readAllNote()
        }

    override suspend fun updateNote(note: NoteEntity) =
        withContext(appCoroutineDispatchers.io) {
            noteDatabase.updateNote(note)
        }

    override suspend fun deleteNote(note: NoteEntity) =
        withContext(appCoroutineDispatchers.io) {
            noteDatabase.deleteNote(note)
        }
}