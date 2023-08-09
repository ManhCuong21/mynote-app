package com.example.domain.usecase

import com.example.core.core.model.NoteUIModel
import com.example.data.repository.NoteRepository
import com.example.domain.mapper.NoteParams
import com.example.domain.mapper.toNoteEntity
import com.example.domain.mapper.toNoteUIModel
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import javax.inject.Inject

class NoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend fun insertNote(note: NoteParams): Result<Unit, Throwable> =
        noteRepository.insertNote(note.toNoteEntity())

    suspend fun readAllNote(): Result<List<NoteUIModel>, Throwable> =
        noteRepository.readAllNote().map { it.map { note -> note.toNoteUIModel() } }

    suspend fun readNoteWithCategory(categoryId: Int): Result<List<NoteUIModel>, Throwable> =
        noteRepository.readNoteWithCategory(categoryId)
            .map {
                it.map { note -> note.toNoteUIModel() }
            }

    suspend fun updateNote(note: NoteUIModel): Result<Unit, Throwable> =
        noteRepository.updateNote(note.toNoteEntity())

    suspend fun deleteNote(note: NoteUIModel): Result<Unit, Throwable> =
        noteRepository.deleteNote(note.toNoteEntity())
}