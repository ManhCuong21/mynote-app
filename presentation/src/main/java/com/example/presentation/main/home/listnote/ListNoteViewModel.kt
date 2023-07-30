package com.example.presentation.main.home.listnote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.base.BaseViewModel
import com.example.core.core.external.ResultContent
import com.example.domain.usecase.NoteUseCase
import com.github.michaelbull.result.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListNoteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val noteUseCase: NoteUseCase
) : BaseViewModel() {
    private val _mutableStateFlow =
        MutableStateFlow(savedStateHandle[STATE_KEY] ?: ListNoteUiState.INITIAL)

    val stateFlow: StateFlow<ListNoteUiState> = _mutableStateFlow.asStateFlow()
    private val _actionSharedFlow = MutableSharedFlow<ListNoteAction>(extraBufferCapacity = 64)
    private inline fun <reified T : ListNoteAction> action() =
        _actionSharedFlow.filterIsInstance<T>()

    private val _singleEventChannel = Channel<ListNoteSingleEvent>(Channel.UNLIMITED).addToBag()
    val singleEventFlow: Flow<ListNoteSingleEvent> get() = _singleEventChannel.receiveAsFlow()

    fun dispatch(action: ListNoteAction) =
        viewModelScope.launch { _actionSharedFlow.emit(action) }

    init {
        getListNote()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getListNote() {
        action<ListNoteAction.GetListNote>()
            .flatMapLatest {
                flow {
                    val database =
                        if (it.category.titleCategory == "All") noteUseCase.readAllNote()
                        else noteUseCase.readNoteWithCategory(it.category.idCategory ?: 0)
                    database.fold(
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
                    is ResultContent.Content -> ListNoteSingleEvent.GetListNote.Success(lce.content)
                    is ResultContent.Error -> ListNoteSingleEvent.GetListNote.Failed(error = lce.error)
                }
                event?.let { _singleEventChannel.send(it) }
            }.launchIn(viewModelScope)
    }

    private companion object {
        private const val STATE_KEY = "ListNoteViewModel.state"
    }
}