package com.example.data.datalocal.database

import com.example.data.datalocal.dao.AppDatabase
import com.example.data.datalocal.model.NoteEntity
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface NoteDatabase {
    suspend fun insertNote(note: NoteEntity): Result<Unit, Throwable>
    suspend fun readAllNote(): Result<List<NoteEntity>, Throwable>
    suspend fun readNoteWithCategory(categoryId: Long): Result<List<NoteEntity>, Throwable>
    suspend fun updateNote(note: NoteEntity): Result<Unit, Throwable>
    suspend fun deleteNote(note: NoteEntity): Result<Unit, Throwable>
    suspend fun searchNoteByTitle(keyword: String): Flow<List<NoteEntity>>
}

class NoteDatabaseImpl @Inject constructor(
    appDatabase: AppDatabase
) : NoteDatabase {
    private val noteDAO = appDatabase.noteDao()
    override suspend fun insertNote(note: NoteEntity): Result<Unit, Throwable> = runCatching {
        noteDAO.insertNote(note)
    }

    override suspend fun readAllNote(): Result<List<NoteEntity>, Throwable> = runCatching {
        noteDAO.readAllNote()
    }

    override suspend fun readNoteWithCategory(categoryId: Long): Result<List<NoteEntity>, Throwable> =
        runCatching {
            noteDAO.readNoteWithCategory(categoryId)
        }

    override suspend fun updateNote(note: NoteEntity): Result<Unit, Throwable> = runCatching {
        noteDAO.updateNote(note)
    }

    override suspend fun deleteNote(note: NoteEntity): Result<Unit, Throwable> = runCatching {
        noteDAO.deleteNote(note)
    }

    override suspend fun searchNoteByTitle(keyword: String): Flow<List<NoteEntity>> {
        return noteDAO.searchNoteByKeyword(keyword)
    }
}