package com.example.presentation.addnote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.base.BaseViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    private val _mutableStateFlow: MutableStateFlow<NoteUiState>
//    val stateFlow: StateFlow<NoteUiState>

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

//        stateFlow = combine(
//
//            ::buildNoteUiState
//        ).onEach {
//            savedStateHandle[STATE_KEY] = it
//        }.stateIn(viewModelScope, SharingStarted.Eagerly, initialUiState)

        updateListImage()
        updateListRecord()
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