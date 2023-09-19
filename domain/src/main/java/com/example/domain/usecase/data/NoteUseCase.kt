package com.example.domain.usecase.data

import com.example.core.core.external.AppConstants.TYPE_REMOTE
import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.model.NoteModel
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.data.datalocal.repository.NoteLocalRepository
import com.example.data.dataremote.repository.NoteRemoteRepository
import com.example.domain.mapper.NoteParams
import com.example.domain.mapper.toNoteEntity
import com.example.domain.mapper.toNoteEntityWithNotification
import com.example.domain.mapper.toNoteModel
import com.example.domain.mapper.toNoteRemote
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.runCatching
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NoteUseCase @Inject constructor(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val sharedPrefersManager: SharedPrefersManager,
    private val noteLocalRepository: NoteLocalRepository,
    private val noteRemoteRepository: NoteRemoteRepository
) {
    suspend fun insertNote(note: NoteParams): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            if (!sharedPrefersManager.userEmail.isNullOrEmpty()) {
                noteRemoteRepository.insertNote(note.toNoteRemote())
            } else {
                noteLocalRepository.insertNote(note.toNoteEntity())
            }
        }

    suspend fun readAllNote(): Result<List<NoteModel>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            if (!sharedPrefersManager.userEmail.isNullOrEmpty()) {
                runCatching {
                    val listRemoteCall = async {
                        noteRemoteRepository.readAllNote().map { flow ->
                            flow.first().map { it.toNoteModel() }
                        }
                    }
                    val listLocalCall = async {
                        noteLocalRepository.readAllNote()
                            .map { list -> list.map { it.toNoteModel() } }
                    }
                    val listRemote = listRemoteCall.await()
                    val listLocal = listLocalCall.await()
                    val listNote = arrayListOf<NoteModel>()
                    val listNoteRemote = when (listRemote) {
                        is Ok -> {
                            listRemote.value
                        }

                        is Err -> {
                            listOf()
                        }
                    }
                    val listNoteLocal = when (listLocal) {
                        is Ok -> {
                            listLocal.value
                        }

                        is Err -> {
                            listOf()
                        }
                    }
                    listNote.addAll(listNoteRemote)
                    listNote.addAll(listNoteLocal)
                    listNote
                }
            } else {
                noteLocalRepository.readAllNote().map { it.map { note -> note.toNoteModel() } }
            }
        }

    suspend fun readNoteWithCategory(idCategory: Long): Result<List<NoteModel>, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            if (!sharedPrefersManager.userEmail.isNullOrEmpty()) {
                runCatching {
                    val listRemoteCall = async {
                        noteRemoteRepository.readNoteWithCategory(idCategory).map { flow ->
                            flow.first().map { it.toNoteModel() }
                        }
                    }
                    val listLocalCall = async {
                        noteLocalRepository.readNoteWithCategory(idCategory)
                            .map { list -> list.map { it.toNoteModel() } }
                    }
                    val listRemote = listRemoteCall.await()
                    val listLocal = listLocalCall.await()
                    val listNote = arrayListOf<NoteModel>()
                    val listNoteRemote = when (listRemote) {
                        is Ok -> {
                            listRemote.value
                        }

                        is Err -> {
                            listOf()
                        }
                    }
                    val listNoteLocal = when (listLocal) {
                        is Ok -> {
                            listLocal.value
                        }

                        is Err -> {
                            listOf()
                        }
                    }
                    listNote.addAll(listNoteRemote)
                    listNote.addAll(listNoteLocal)
                    listNote
                }
            } else {
                noteLocalRepository.readNoteWithCategory(idCategory)
                    .map { it.map { note -> note.toNoteModel() } }
            }
        }

    suspend fun updateNote(note: NoteModel): Result<Unit, Throwable> =
        withContext(appCoroutineDispatchers.io) {
            if (!sharedPrefersManager.userEmail.isNullOrEmpty()) {
                noteRemoteRepository.updateNote(note.toNoteRemote())
            } else {
                noteLocalRepository.updateNote(note.toNoteEntity())
            }
        }

    suspend fun updateNotificationNote(note: NoteModel): Result<Unit, Throwable> =
        if (!sharedPrefersManager.userEmail.isNullOrEmpty()) {
            noteRemoteRepository.updateNote(note.toNoteRemote())
        } else {
            noteLocalRepository.updateNote(note.toNoteEntityWithNotification())
        }

    suspend fun deleteNote(note: NoteModel): Result<Unit, Throwable> =
        if (!sharedPrefersManager.userEmail.isNullOrEmpty() && note.typeNote == TYPE_REMOTE) {
            noteRemoteRepository.deleteNote(note.toNoteRemote())
        } else {
            noteLocalRepository.deleteNote(note.toNoteEntity())
        }
}