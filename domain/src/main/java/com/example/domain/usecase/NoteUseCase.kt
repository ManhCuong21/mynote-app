package com.example.domain.usecase

import com.example.data.model.NoteEntity
import com.example.data.repository.NoteRepository
import com.github.michaelbull.result.Result
import javax.inject.Inject

class NoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend fun insertNote(note: NoteEntity): Result<Unit, Throwable> =
        noteRepository.insertNote(note)

    suspend fun readListNote(): Result<List<NoteEntity>, Throwable> = noteRepository.readNote()

    suspend fun updateNote(note: NoteEntity): Result<Unit, Throwable> =
        noteRepository.updateNote(note)

    suspend fun deleteNote(note: NoteEntity): Result<Unit, Throwable> =
        noteRepository.deleteNote(note)
}