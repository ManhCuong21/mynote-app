package com.example.mynote.ui.addnote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.mynote.base.BaseViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class NoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
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

        val permissionCameraFlow = action<NoteAction.CameraPermissionResult>()
            .map { it.isGranted }
            .onStart { emit(initialUiState.permissionCameraGranted) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed()).distinctUntilChanged()

        val permissionStorageFlow = action<NoteAction.StoragePermissionResult>()
            .map { it.isGranted }
            .onStart { emit(initialUiState.permissionCameraGranted) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed()).distinctUntilChanged()

        val listImageFlow = action<NoteAction.UpdateListImage>()
            .map { it.image }
            .onStart { emit(initialUiState.listImage) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed()).distinctUntilChanged()

        stateFlow = combine(
            permissionCameraFlow,
            permissionStorageFlow,
            ::buildNoteUiState
        ).onEach {
            savedStateHandle[STATE_KEY] = it
        }.stateIn(viewModelScope, SharingStarted.Eagerly, initialUiState)

        updatePermission()
    }

    private fun updatePermission() {
//        action<NoteAction.CameraPermissionResult>()
//            .map { it.isGranted }
//            .onEach {
//                _mutableStateFlow.update { state ->
//                    state.copy(permissionCameraGranted = it)
//                }
//            }.launchIn(viewModelScope)
//
//        action<NoteAction.StoragePermissionResult>()
//            .map { it.isGranted }
//            .onEach {
//                _mutableStateFlow.update { state ->
//                    state.copy(permissionStorageGranted = it)
//                }
//            }.launchIn(viewModelScope)
    }

    private companion object {
        private const val STATE_KEY = "NoteViewModel.state"
    }
}