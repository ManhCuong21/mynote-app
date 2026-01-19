package com.example.data.datalocal.repository

import com.example.core.core.external.AppCoroutineDispatchers
import com.example.data.datalocal.database.NoteDatabase
import com.example.data.datalocal.model.NoteEntity
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface NoteRepository {
    suspend fun insertNote(note: NoteEntity): Result<Unit, Throwable>
    suspend fun readAllNote(): Result<List<NoteEntity>, Throwable>
    suspend fun readNoteWithCategory(idCategory: Long): Result<List<NoteEntity>, Throwable>
    suspend fun updateNote(note: NoteEntity): Result<Unit, Throwable>
    suspend fun deleteNote(note: NoteEntity): Result<Unit, Throwable>
    suspend fun searchNoteByTitle(keyword: String): Flow<List<NoteEntity>>
    suspend fun updateNoteFolder(noteId: Long, newFolderDir: String)
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

    override suspend fun readNoteWithCategory(idCategory: Long): Result<List<NoteEntity>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            noteDatabase.readNoteWithCategory(idCategory)
        }

    override suspend fun updateNote(note: NoteEntity) =
        withContext(appCoroutineDispatchers.io) {
            noteDatabase.updateNote(note)
        }

    override suspend fun deleteNote(note: NoteEntity) =
        withContext(appCoroutineDispatchers.io) {
            noteDatabase.deleteNote(note)
        }

    override suspend fun searchNoteByTitle(keyword: String): Flow<List<NoteEntity>> =
        noteDatabase.searchNoteByTitle(keyword)

    override suspend fun updateNoteFolder(noteId: Long, newFolderDir: String) {
        noteDatabase.updateNoteFolder(noteId, newFolderDir)
    }
}