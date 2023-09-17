package com.example.domain.usecase.data

import com.example.core.core.model.NoteModel
import com.example.data.datalocal.repository.NoteRepository
import com.example.domain.mapper.NoteParams
import com.example.domain.mapper.toNoteEntity
import com.example.domain.mapper.toNoteEntityWithNotification
import com.example.domain.mapper.toNoteModel
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import javax.inject.Inject

class NoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend fun insertNote(note: NoteParams): Result<Unit, Throwable> =
        noteRepository.insertNote(note.toNoteEntity())

    suspend fun readAllNote(): Result<List<NoteModel>, Throwable> =
        noteRepository.readAllNote().map { it.map { note -> note.toNoteModel() } }

    suspend fun readNoteWithCategory(idCategory: Long): Result<List<NoteModel>, Throwable> =
        noteRepository.readNoteWithCategory(idCategory)
            .map { it.map { note -> note.toNoteModel() } }

    suspend fun updateNote(note: NoteModel): Result<Unit, Throwable> =
        noteRepository.updateNote(note.toNoteEntity())


    suspend fun updateNotificationNote(note: NoteModel): Result<Unit, Throwable> =
        noteRepository.updateNote(note.toNoteEntityWithNotification())

    suspend fun deleteNote(note: NoteModel): Result<Unit, Throwable> =
        noteRepository.deleteNote(note.toNoteEntity())
}