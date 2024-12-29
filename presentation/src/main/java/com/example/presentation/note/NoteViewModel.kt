package com.example.presentation.note

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.base.BaseViewModel
import com.example.core.core.external.ActionNote
import com.example.core.core.external.AppConstants.TYPE_LOCAL
import com.example.core.core.external.AppConstants.TYPE_REMOTE
import com.example.core.core.external.ResultContent
import com.example.core.core.external.combine
import com.example.core.core.model.NoteModel
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.domain.mapper.NoteParams
import com.example.domain.usecase.data.FirebaseStorageUseCase
import com.example.domain.usecase.data.NoteUseCase
import com.example.domain.usecase.file.FileUseCase
import com.example.domain.usecase.file.ImageFileUseCase
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteUseCase: NoteUseCase,
    private val fileUseCase: FileUseCase,
    private val imageFileUseCase: ImageFileUseCase,
    private val recordFileUseCase: RecordFileUseCase,
    private val firebaseStorageUseCase: FirebaseStorageUseCase,
    private val sharedPrefersManager: SharedPrefersManager
) : BaseViewModel() {
    private val _mutableStateFlow: MutableStateFlow<NoteUiState>
    val stateFlow: StateFlow<NoteUiState>
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
        _mutableStateFlow = MutableStateFlow(initialUiState).apply {
            onEach { savedStateHandle[STATE_KEY] = it }.launchIn(viewModelScope)
        }
        stateFlow = _mutableStateFlow.asStateFlow()

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
                        typeNote = noteModel.typeNote,
                        notificationModel = noteModel.notificationModel,
                        security = noteModel.security
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

    private fun saveMediaFlow(context: FragmentActivity, noteModel: NoteModel?) =
        flow {
            emit(ResultContent.Loading)
            val type = noteModel?.typeNote
                ?: if (sharedPrefersManager.userEmail.isNullOrEmpty()) TYPE_LOCAL else TYPE_REMOTE
            if (type == TYPE_REMOTE) {
                firebaseStorageUseCase.deleteDirectory(uiStateFlow.value.directoryName.orEmpty())
                firebaseStorageUseCase.saveFile(
                    context,
                    uiStateFlow.value.directoryName.orEmpty()
                )
            } else {
                imageFileUseCase.saveImageToDirectory(
                    context,
                    uiStateFlow.value.directoryName.orEmpty()
                )
                recordFileUseCase.saveRecordToDirectory(
                    context,
                    uiStateFlow.value.directoryName.orEmpty()
                )
            }.fold(
                success = {
                    ResultContent.Content(it)
                },
                failure = {
                    ResultContent.Error(it)
                }
            ).let { emit(it) }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun saveNote() {
        action<NoteAction.SaveNote>()
            .flatMapLatest {
                saveMediaFlow(it.context, it.noteModel).zip(
                    if (it.action == ActionNote.UPDATE_NOTE) updateNoteFlow(it.noteModel) else insertNoteFlow()
                ) { result, _ ->
                    result
                }
            }.onEach { result ->
                val event = when (result) {
                    is ResultContent.Loading -> null
                    is ResultContent.Content -> NoteSingleEvent.SaveNoteSuccess
                    is ResultContent.Error -> NoteSingleEvent.Failed(error = result.error)
                }
                _mutableStateFlow.update { state ->
                    state.copy(isLoading = result is ResultContent.Loading)
                }
                event?.let { _singleEventChannel.send(it) }
            }.launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun saveFileMediaToTemp() {
        action<NoteAction.SaveFileMediaToTemp>()
            .flatMapLatest {
                flow {
                    emit(ResultContent.Loading)
                    val saveFile = if (it.noteModel.typeNote == TYPE_REMOTE) {
                        firebaseStorageUseCase.saveListFileToTemp(
                            it.context,
                            it.noteModel.nameMediaNote
                        )
                    } else {
                        imageFileUseCase.saveImageFromDirectoryToTemp(
                            fragmentActivity = it.context,
                            directoryName = it.noteModel.nameMediaNote
                        )
                        recordFileUseCase.saveRecordFromDirectoryToTemp(
                            fragmentActivity = it.context,
                            directoryName = it.noteModel.nameMediaNote
                        )
                    }
                    saveFile.fold(
                        success = {
                            ResultContent.Content(it)
                        },
                        failure = {
                            ResultContent.Error(it)
                        }
                    ).let { emit(it) }
                }
            }.onEach { result ->
                when (result) {
                    is ResultContent.Loading -> null
                    is ResultContent.Content -> NoteSingleEvent.SaveFileToTempSuccess
                    is ResultContent.Error -> NoteSingleEvent.Failed(error = result.error)
                }.let { event -> event?.let { _singleEventChannel.send(it) } }
                _mutableStateFlow.update { state ->
                    state.copy(isLoading = result is ResultContent.Loading)
                }
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
                fileUseCase.deleteDirectory(it.context, uiStateFlow.value.directoryName.orEmpty())
            }.launchIn(viewModelScope)
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
                recordFileUseCase.deleteRecord(it.recordPath)
                dispatch(NoteAction.GetListRecordNote(it.context))
            }
            .launchIn(viewModelScope)
    }

    private companion object {
        private const val STATE_KEY = "NoteViewModel.state"
    }
}