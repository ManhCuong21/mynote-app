package com.example.mynote.data.database

import com.example.mynote.data.dao.AppDAO
import com.example.mynote.data.model.NoteEntity
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import javax.inject.Inject

interface NoteDatabase {
    suspend fun insertNote(note: NoteEntity): Result<Unit, Throwable>
    suspend fun readAllNote(): Result<List<NoteEntity>, Throwable>
    suspend fun updateNote(note: NoteEntity): Result<Unit, Throwable>
    suspend fun deleteNote(note: NoteEntity): Result<Unit, Throwable>
}

class NoteDatabaseImpl @Inject constructor(
    private val appDao: AppDAO
) : NoteDatabase {
    private val noteDAO = appDao.noteDao()
    override suspend fun insertNote(note: NoteEntity): Result<Unit, Throwable> = runCatching {
        noteDAO.insertNote(note)
    }

    override suspend fun readAllNote(): Result<List<NoteEntity>, Throwable> = runCatching {
        noteDAO.readAllNote()
    }

    override suspend fun updateNote(note: NoteEntity): Result<Unit, Throwable> = runCatching {
        noteDAO.updateNote(note)
    }

    override suspend fun deleteNote(note: NoteEntity): Result<Unit, Throwable> = runCatching {
        noteDAO.deleteNote(note)
    }

}