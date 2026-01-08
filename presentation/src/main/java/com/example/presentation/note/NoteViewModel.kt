package com.example.presentation.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.base.BaseViewModel
import com.example.core.core.external.ActionNote
import com.example.core.core.external.ResultContent
import com.example.core.core.external.combine
import com.example.core.core.model.NoteModel
import com.example.domain.mapper.NoteParams
import com.example.domain.usecase.data.NoteUseCase
import com.example.domain.usecase.file.FileUseCase
import com.example.domain.usecase.file.RecordFileUseCase
import com.github.michaelbull.result.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val noteUseCase: NoteUseCase,
    private val fileUseCase: FileUseCase,
    private val recordFileUseCase: RecordFileUseCase
) : BaseViewModel() {
    private val _loadingStateFlow = MutableStateFlow(false)
    val loadingStateFlow: StateFlow<Boolean> = _loadingStateFlow.asStateFlow()
    val uiStateFlow: StateFlow<NoteUiState>

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
        val isFirstFlow = action<NoteAction.IsFirstTime>()
            .map { true }
            .onStart { emit(initialUiState.isFirstTime) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

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

        val securityNoteFlow = action<NoteAction.SecurityNoteChanged>()
            .map { it.security }
            .onStart { emit(initialUiState.security ?: false) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val isFirstTime = isFirstFlow.distinctUntilChanged()
        val titleNote = titleNoteFlow.distinctUntilChanged()
        val contentNote = contentNoteFlow.distinctUntilChanged()
        val categoryNote = categoryNoteFlow.distinctUntilChanged()
        val fileMediaNote = fileMediaNoteFlow.distinctUntilChanged()
        val hasImageNote = hasImageNoteFlow.distinctUntilChanged()
        val hasRecordNote = hasRecordNoteFlow.distinctUntilChanged()
        val colorTitleNote = colorTitleNoteFlow.distinctUntilChanged()
        val colorContentNote = colorContentNoteFlow.distinctUntilChanged()
        val securityNote = securityNoteFlow.distinctUntilChanged()

        uiStateFlow = combine(
            isFirstTime,
            titleNote,
            contentNote,
            categoryNote,
            fileMediaNote,
            hasImageNote,
            hasRecordNote,
            colorTitleNote,
            colorContentNote,
            securityNote,
            ::buildNoteUiState
        ).onEach {
            savedStateHandle[STATE_KEY] = it
        }.stateIn(viewModelScope, SharingStarted.Eagerly, initialUiState)

        deleteDirectory()
        deleteDirectoryTemp()
        saveFileMediaToTemp()
        saveNote()
        getListRecord()
        deleteRecord()
    }

    private fun insertNoteFlow() =
        flow {
            emit(ResultContent.Loading)
            val uiState = uiStateFlow.value
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
                    timeNote = System.currentTimeMillis(),
                    security = uiState.security ?: false
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

    private fun updateNoteFlow(noteModel: NoteModel?) =
        flow {
            emit(ResultContent.Loading)
            val uiState = uiStateFlow.value
            if (noteModel != null) {
                noteUseCase.updateNote(
                    NoteModel(
                        idNote = noteModel.idNote,
                        titleNote = uiState.titleNote.orEmpty(),
                        contentNote = uiState.contentNote.orEmpty(),
                        categoryNote = uiState.categoryNote,
                        nameMediaNote = uiState.directoryName.orEmpty(),
                        hasImage = uiState.hasImage ?: false,
                        hasRecord = uiState.hasRecord ?: false,
                        colorTitleNote = uiState.colorTitleNote.orEmpty(),
                        colorContentNote = uiState.colorContentNote.orEmpty(),
                        timeNote = System.currentTimeMillis(),
                        notificationModel = noteModel.notificationModel,
                        security = uiState.security ?: false
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
        }

    private fun saveMediaFlow() = flow {
        emit(ResultContent.Loading)
        val uiState = uiStateFlow.value
        val directoryName = uiState.directoryName.orEmpty()

        if (directoryName.isEmpty()) {
            emit(ResultContent.Error(Exception("Directory name is empty")))
            return@flow
        }

        try {
            fileUseCase.saveFileToDirectory(directoryName)
            emit(ResultContent.Content(Unit))
        } catch (e: Exception) {
            emit(ResultContent.Error(e))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun saveNote() {
        action<NoteAction.SaveNote>()
            .flatMapMerge { action ->
                flow {
                    emit(ResultContent.Loading)

                    val mediaFlow = saveMediaFlow()
                    val noteFlow = if (action.action == ActionNote.UPDATE_NOTE)
                        updateNoteFlow(action.noteModel)
                    else
                        insertNoteFlow()

                    val resultFlow = mediaFlow.zip(noteFlow) { mediaResult, noteResult ->
                        when {
                            mediaResult is ResultContent.Error -> ResultContent.Error(mediaResult.error)
                            noteResult is ResultContent.Error -> ResultContent.Error(noteResult.error)
                            mediaResult is ResultContent.Content && noteResult is ResultContent.Content ->
                                ResultContent.Content(Unit)

                            else -> ResultContent.Loading
                        }
                    }

                    emitAll(resultFlow)
                }
            }
            .onEach { result ->
                updateLoading(result)
                when (result) {
                    is ResultContent.Loading -> Unit
                    is ResultContent.Content -> _singleEventChannel.send(NoteSingleEvent.SaveNoteSuccess)
                    is ResultContent.Error -> _singleEventChannel.send(
                        NoteSingleEvent.Failed(error = result.error)
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun saveFileMediaToTemp() {
        action<NoteAction.SaveFileMediaToTemp>()
            .flatMapLatest { action ->
                flow {
                    emit(ResultContent.Loading)
                    try {
                        val directoryName = action.noteModel.nameMediaNote
                        fileUseCase.saveFileToTemp(directoryName)
                        emit(ResultContent.Content(Unit))
                    } catch (e: Throwable) {
                        emit(ResultContent.Error(e))
                    }
                }
            }
            .onEach { result ->
                updateLoading(result)
                when (result) {
                    is ResultContent.Loading -> Unit
                    is ResultContent.Content -> _singleEventChannel.send(NoteSingleEvent.SaveFileToTempSuccess)
                    is ResultContent.Error -> _singleEventChannel.send(NoteSingleEvent.Failed(result.error))
                }
            }
            .launchIn(viewModelScope)
    }

    private fun deleteDirectoryTemp() {
        action<NoteAction.DeleteDirectoryTemp>()
            .onEach {
                fileUseCase.deleteDirectoryTemp()
            }.launchIn(viewModelScope)
    }

    private fun deleteDirectory() {
        action<NoteAction.DeleteDirectory>()
            .onEach {
                fileUseCase.deleteDirectory(uiStateFlow.value.directoryName.orEmpty())
            }.launchIn(viewModelScope)
    }

    private fun getListRecord() {
        action<NoteAction.GetListRecordNote>()
            .onEach {
                val listRecord = recordFileUseCase.getListRecord()
                _singleEventChannel.send(NoteSingleEvent.GetListRecord(listRecord))
            }.launchIn(viewModelScope)
    }

    private fun deleteRecord() {
        action<NoteAction.DeleteRecordNote>()
            .onEach {
                recordFileUseCase.deleteRecord(it.recordPath)
                dispatch(NoteAction.GetListRecordNote)
            }
            .launchIn(viewModelScope)
    }

    private fun updateLoading(result: ResultContent<Unit>) {
        _loadingStateFlow.update { result is ResultContent.Loading }
    }

    private companion object {
        private const val STATE_KEY = "NoteViewModel.state"
    }
}