package com.example.presentation.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.base.BaseViewModel
import com.example.core.core.external.ResultContent
import com.example.core.core.model.NoteUIModel
import com.example.domain.usecase.NoteUseCase
import com.github.michaelbull.result.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    private val noteUseCase: NoteUseCase
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
        val initialUiState =
            savedStateHandle.get<NoteUiState?>(STATE_KEY)?.copy()
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
            .onStart { emit(initialUiState.categoryId ?: 0) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val fileMediaNoteFlow = action<NoteAction.FileMediaNoteChanged>()
            .map { it.fileMediaNote }
            .onStart { emit(initialUiState.fileMediaNote.orEmpty()) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val colorNoteFlow = action<NoteAction.ColorNoteChanged>()
            .map { it.indexColor }
            .onStart { emit(initialUiState.colorNote ?: 0) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val titleNote = titleNoteFlow.distinctUntilChanged()
        val contentNote = contentNoteFlow.distinctUntilChanged()
        val categoryNote = categoryNoteFlow.distinctUntilChanged()
        val fileMediaNote = fileMediaNoteFlow.distinctUntilChanged()
        val colorNote = colorNoteFlow.distinctUntilChanged()

        stateFlow = combine(
            titleNote,
            contentNote,
            categoryNote,
            fileMediaNote,
            colorNote,
            ::buildNoteUiState
        ).onEach {
            savedStateHandle[STATE_KEY] = it
        }.stateIn(viewModelScope, SharingStarted.Eagerly, initialUiState)

        updateListImage()
        updateListRecord()
        saveNote()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun saveNote() {
        action<NoteAction.InsertNote>()
            .flatMapLatest {
                flow {
                    emit(ResultContent.Loading)
                    val uiState = stateFlow.value
                    noteUseCase.insertNote(
                        NoteUIModel(
                            titleNote = uiState.titleNote.orEmpty(),
                            contentNote = uiState.contentNote.orEmpty(),
                            categoryId = uiState.categoryId ?: 0,
                            fileMediaNote = uiState.fileMediaNote.orEmpty(),
                            colorNote = uiState.colorNote ?: 0,
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

    private fun updateListImage() {
        action<NoteAction.UpdateListImage>()
            .onEach { _singleEventChannel.send(NoteSingleEvent.UpdateListImage) }
            .launchIn(viewModelScope)
    }

    private fun updateListRecord() {
        action<NoteAction.UpdateListRecord>()
            .onEach { _singleEventChannel.send(NoteSingleEvent.UpdateListRecord) }
            .launchIn(viewModelScope)
    }

    private companion object {
        private const val STATE_KEY = "NoteViewModel.state"
    }
}