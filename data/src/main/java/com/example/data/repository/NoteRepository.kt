package com.example.data.repository

import com.example.core.core.external.AppCoroutineDispatchers
import com.example.data.model.NoteEntity
import com.example.data.database.NoteDatabase
import com.example.data.model.CategoryEntity
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.github.michaelbull.result.Result

interface NoteRepository {
    suspend fun insertNote(note: NoteEntity): Result<Unit, Throwable>
    suspend fun readAllNote(): Result<List<NoteEntity>, Throwable>
    suspend fun readNoteWithCategory(categoryId: Int): Result<List<NoteEntity>, Throwable>
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

    override suspend fun readAllNote(): Result<List<NoteEntity>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            noteDatabase.readAllNote()
        }

    override suspend fun readNoteWithCategory(categoryId: Int): Result<List<NoteEntity>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            noteDatabase.readNoteWithCategory(categoryId)
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