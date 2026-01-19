package com.example.domain.usecase.data

import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.model.NoteModel
import com.example.data.datalocal.repository.NoteRepository
import com.example.domain.mapper.NoteParams
import com.example.domain.mapper.toNoteEntity
import com.example.domain.mapper.toNoteEntityWithNotification
import com.example.domain.mapper.toNoteModel
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NoteUseCase @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val noteRepository: NoteRepository,
) {
    suspend fun insertNote(note: NoteParams): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            noteRepository.insertNote(note.toNoteEntity())
        }

    suspend fun readAllNote(): Result<List<NoteModel>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            noteRepository.readAllNote()
                .map { list -> list.map { it.toNoteModel() } }
        }

    suspend fun readNoteWithCategory(idCategory: Long): Result<List<NoteModel>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            noteRepository.readNoteWithCategory(idCategory)
                .map { it.map { note -> note.toNoteModel() } }
        }

    suspend fun updateNote(note: NoteModel): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            noteRepository.updateNote(note.toNoteEntity())
        }

    suspend fun updateNotificationNote(note: NoteModel): Result<Unit, Throwable> =
        noteRepository.updateNote(note.toNoteEntityWithNotification())

    suspend fun deleteNote(note: NoteModel): Result<Unit, Throwable> =
        noteRepository.deleteNote(note.toNoteEntity())


    suspend fun searchNoteByTitle(keyword: String): Flow<List<NoteModel>> =
        noteRepository.searchNoteByTitle(keyword)
            .map { list -> list.map { it.toNoteModel() } }
}