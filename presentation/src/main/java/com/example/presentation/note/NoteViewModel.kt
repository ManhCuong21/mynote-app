package com.example.presentation.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.base.BaseViewModel
import com.example.core.core.external.ResultContent
import com.example.core.core.external.combine
import com.example.core.core.model.NoteModel
import com.example.domain.mapper.NoteParams
import com.example.domain.usecase.file.FileUseCase
import com.example.domain.usecase.file.ImageFileUseCase
import com.example.domain.usecase.file.RecordFileUseCase
import com.example.domain.usecase.local.NoteUseCase
import com.github.michaelbull.result.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteUseCase: NoteUseCase,
    private val fileUseCase: FileUseCase,
    private val imageFileUseCase: ImageFileUseCase,
    private val recordFileUseCase: RecordFileUseCase
) : BaseViewModel() {
    private val _mutableStateFlow: MutableStateFlow<NoteUiState>
    val stateFlow: StateFlow<NoteUiState>

    private val _actionSharedFlow = MutableSharedFlow<NoteAction>(extraBufferCapacity = 64)
    private inline fun <reified T : NoteAction> action() =
        _actionSharedFlow.filterIsInstance<T>()

    private val _singleEventChannel = Channel<NoteSingleEvent>(Channel.UNLIMITED).addToBag()
    val singleEventFlow: Flow<NoteSingleEvent> get() = _singleEventChannel.receiveAsFlow()

    fun dispatch(action: NoteAction) =
        viewModelScope.launch { _actionSharedFlow.emit(action) }

    init {
        val initialUiState = savedStateHandle.get<NoteUiState?>(STATE_KEY)?.copy()
            ?: NoteUiState.INITIAL
        _mutableStateFlow = MutableStateFlow(initialUiState).apply {
            onEach { savedStateHandle[STATE_KEY] = it }.launchIn(viewModelScope)
        }
        val titleNoteFlow = action<NoteAction.TitleNoteChanged>()
            .map { it.titleNote }
            .onStart { emit(initialUiState.titleNote.orEmpty()) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val contentNoteFlow = action<NoteAction.ContentNoteChanged>()
            .map { it.contentNote }
            .onStart { emit(initialUiState.contentNote.orEmpty()) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val categoryNoteFlow = action<NoteAction.CategoryNoteChanged>()
            .map { it.categoryNote }
            .onStart { emit(initialUiState.categoryNote) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val fileMediaNoteFlow = action<NoteAction.DirectoryNameNoteChanged>()
            .map { it.fileMediaNote }
            .onStart { emit(initialUiState.directoryName.orEmpty()) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val hasImageNoteFlow = action<NoteAction.HasImageNoteChanged>()
            .map { it.hasImage }
            .onStart { emit(initialUiState.hasImage ?: false) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val hasRecordNoteFlow = action<NoteAction.HasRecordNoteChanged>()
            .map { it.hasRecord }
            .onStart { emit(initialUiState.hasRecord ?: false) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val colorTitleNoteFlow = action<NoteAction.ColorTitleNoteChanged>()
            .map { it.colorTitleNote }
            .onStart { emit(initialUiState.colorTitleNote.orEmpty()) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val colorContentNoteFlow = action<NoteAction.ColorContentNoteChanged>()
            .map { it.colorContentNote }
            .onStart { emit(initialUiState.colorContentNote.orEmpty()) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val notificationNoteFlow = action<NoteAction.NotificationNoteChanged>()
            .map { it.notificationModel }
            .onStart { emit(initialUiState.notificationModel) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val titleNote = titleNoteFlow.distinctUntilChanged()
        val contentNote = contentNoteFlow.distinctUntilChanged()
        val categoryNote = categoryNoteFlow.distinctUntilChanged()
        val fileMediaNote = fileMediaNoteFlow.distinctUntilChanged()
        val hasImageNote = hasImageNoteFlow.distinctUntilChanged()
        val hasRecordNote = hasRecordNoteFlow.distinctUntilChanged()
        val colorTitleNote = colorTitleNoteFlow.distinctUntilChanged()
        val colorContentNote = colorContentNoteFlow.distinctUntilChanged()
        val notificationNote = notificationNoteFlow.distinctUntilChanged()

        stateFlow = combine(
            titleNote,
            contentNote,
            categoryNote,
            fileMediaNote,
            hasImageNote,
            hasRecordNote,
            colorTitleNote,
            colorContentNote,
            notificationNote,
            ::buildNoteUiState
        ).onEach {
            savedStateHandle[STATE_KEY] = it
        }.stateIn(viewModelScope, SharingStarted.Eagerly, initialUiState)

        deleteDirectory()
        deleteDirectoryTemp()
        saveFileMediaToTemp()
        saveMediaToDirectory()
        saveImageNote()
        getListImage()
        deleteImage()
        getListRecord()
        deleteRecord()
        saveNote()
        updateNote()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun saveNote() {
        action<NoteAction.InsertNote>()
            .flatMapLatest {
                flow {
                    emit(ResultContent.Loading)
                    dispatch(NoteAction.SaveMediaToDirectory(it.context))
                    val uiState = stateFlow.value
                    noteUseCase.insertNote(
                        NoteParams(
                            titleNote = uiState.titleNote.orEmpty(),
                            contentNote = uiState.contentNote.orEmpty(),
                            categoryNote = uiState.categoryNote,
                            fileMediaNote = uiState.directoryName.orEmpty(),
                            hasImage = uiState.hasImage ?: false,
                            hasRecord = uiState.hasRecord ?: false,
                            colorTitleNote = uiState.colorTitleNote.orEmpty(),
                            colorContentNote = uiState.colorContentNote.orEmpty(),
                            timeNote = System.currentTimeMillis()
                        )
                    ).fold(
                        success = {
                            ResultContent.Content(it)
                        },
                        failure = {
                            ResultContent.Error(it)
                        }
                    ).let { emit(it) }
                }
            }.onEach { lce ->
                val event = when (lce) {
                    is ResultContent.Loading -> null
                    is ResultContent.Content -> NoteSingleEvent.SaveNote.Success
                    is ResultContent.Error -> NoteSingleEvent.SaveNote.Failed(error = lce.error)
                }
                event?.let { _singleEventChannel.send(it) }
            }.launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun updateNote() {
        action<NoteAction.UpdateNote>()
            .flatMapLatest {
                flow {
                    emit(ResultContent.Loading)
                    val uiState = stateFlow.value
                    noteUseCase.updateNote(
                        NoteModel(
                            idNote = it.noteModel.idNote,
                            titleNote = uiState.titleNote.orEmpty(),
                            contentNote = uiState.contentNote.orEmpty(),
                            categoryNote = uiState.categoryNote,
                            nameMediaNote = uiState.directoryName.orEmpty(),
                            hasImage = uiState.hasImage ?: false,
                            hasRecord = uiState.hasRecord ?: false,
                            colorTitleNote = uiState.colorTitleNote.orEmpty(),
                            colorContentNote = uiState.colorContentNote.orEmpty(),
                            notificationModel = uiState.notificationModel,
                            timeNote = System.currentTimeMillis()
                        )
                    ).fold(
                        success = {
                            ResultContent.Content(it)
                        },
                        failure = {
                            ResultContent.Error(it)
                        }
                    ).let { emit(it) }
                }
            }.onEach { lce ->
                val event = when (lce) {
                    is ResultContent.Loading -> null
                    is ResultContent.Content -> NoteSingleEvent.SaveNote.Success
                    is ResultContent.Error -> NoteSingleEvent.SaveNote.Failed(error = lce.error)
                }
                event?.let { _singleEventChannel.send(it) }
            }.launchIn(viewModelScope)
    }

    private fun saveFileMediaToTemp() {
        action<NoteAction.SaveFileMediaToTemp>()
            .onEach {
                fileUseCase.saveFileToTemp(it.context, it.directoryName)
            }.launchIn(viewModelScope)
    }

    private fun saveMediaToDirectory() {
        action<NoteAction.SaveMediaToDirectory>()
            .onEach {
                fileUseCase.saveFileToDirectory(it.context, stateFlow.value.directoryName.orEmpty())
            }.launchIn(viewModelScope)
    }

    private fun deleteDirectoryTemp() {
        action<NoteAction.DeleteDirectoryTemp>()
            .onEach {
                fileUseCase.deleteDirectoryTemp(it.context)
            }.launchIn(viewModelScope)
    }

    private fun deleteDirectory() {
        action<NoteAction.DeleteDirectory>()
            .onEach {
                fileUseCase.deleteDirectory(
                    fileUseCase.getOutputMediaDirectory(
                        it.context,
                        stateFlow.value.directoryName.orEmpty()
                    )
                )
            }.launchIn(viewModelScope)
    }

    private fun saveImageNote() {
        action<NoteAction.SaveImageNote>()
            .onEach {
                imageFileUseCase.saveImageToTemp(it.context, it.bitmap)
                dispatch(NoteAction.GetListImageNote(it.context))
            }.launchIn(viewModelScope)
    }

    private fun getListImage() {
        action<NoteAction.GetListImageNote>()
            .onEach {
                val listImage = imageFileUseCase.readImage(it.context)
                dispatch(NoteAction.HasImageNoteChanged(listImage.isNotEmpty()))
                _singleEventChannel.send(NoteSingleEvent.GetListImage(listImage))
            }.launchIn(viewModelScope)
    }

    private fun deleteImage() {
        action<NoteAction.DeleteImageNote>()
            .onEach {
                imageFileUseCase.deleteImage(it.pathImage)
                dispatch(NoteAction.GetListImageNote(it.context))
            }
            .launchIn(viewModelScope)
    }

    private fun getListRecord() {
        action<NoteAction.GetListRecordNote>()
            .onEach {
                val listRecord = recordFileUseCase.readRecord(it.context)
                dispatch(NoteAction.HasRecordNoteChanged(listRecord.isNotEmpty()))
                _singleEventChannel.send(NoteSingleEvent.GetListRecord(listRecord))
            }.launchIn(viewModelScope)
    }

    private fun deleteRecord() {
        action<NoteAction.DeleteRecordNote>()
            .onEach {
                recordFileUseCase.deleteRecord(it.pathRecord)
                dispatch(NoteAction.GetListRecordNote(it.context))
            }
            .launchIn(viewModelScope)
    }

    private companion object {
        private const val STATE_KEY = "NoteViewModel.state"
    }
}