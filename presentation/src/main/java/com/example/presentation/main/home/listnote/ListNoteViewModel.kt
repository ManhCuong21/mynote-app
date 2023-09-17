package com.example.presentation.main.home.listnote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.base.BaseViewModel
import com.example.core.core.external.ResultContent
import com.example.domain.usecase.file.FileUseCase
import com.example.domain.usecase.data.CategoryUseCase
import com.example.domain.usecase.data.NoteUseCase
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListNoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val categoryUseCase: CategoryUseCase,
    private val noteUseCase: NoteUseCase,
    private val fileUseCase: FileUseCase
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
        getListCategory()
        getListNote()
        changeCategoryNote()
        deleteNote()
    }

    private fun getListCategory() {
        flow {
            emit(ResultContent.Loading)
            categoryUseCase.readAllCategory().fold(
                success = {
                    ResultContent.Content(it)
                },
                failure = {
                    ResultContent.Error(it)
                }
            ).let { emit(it) }
        }.onEach { result ->
            when (result) {
                is ResultContent.Loading -> {}
                is ResultContent.Content -> _mutableStateFlow.update { state ->
                    state.copy(listCategory = result.content)
                }

                is ResultContent.Error -> ListNoteSingleEvent.SingleEventFailed(error = result.error)
            }
        }.launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getListNote() {
        action<ListNoteAction.GetListNote>()
            .flatMapLatest {
                flow {
                    val database =
                        if (it.category.titleCategory == "All") noteUseCase.readAllNote()
                        else noteUseCase.readNoteWithCategory(it.category.idCategory)
                    database.fold(
                        success = {
                            ResultContent.Content(it)
                        },
                        failure = {
                            ResultContent.Error(it)
                        }
                    ).let { emit(it) }
                }
            }.onEach { result ->
                val event = when (result) {
                    is ResultContent.Loading -> null
                    is ResultContent.Content -> ListNoteSingleEvent.GetListNoteSuccess(result.content)
                    is ResultContent.Error -> ListNoteSingleEvent.SingleEventFailed(error = result.error)
                }
                event?.let { _singleEventChannel.send(it) }
            }.launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun changeCategoryNote() {
        action<ListNoteAction.ChangeCategoryNote>()
            .flatMapLatest {
                flow {
                    noteUseCase.updateNote(it.noteModel.copy(categoryNote = it.category))
                        .fold(
                            success = {
                                ResultContent.Content(it)
                            },
                            failure = {
                                ResultContent.Error(it)
                            }
                        ).let { emit(it) }
                }
            }.onEach { result ->
                val event = when (result) {
                    is ResultContent.Loading -> null
                    is ResultContent.Content -> ListNoteSingleEvent.UpdateNote
                    is ResultContent.Error -> ListNoteSingleEvent.SingleEventFailed(error = result.error)
                }
                event?.let { _singleEventChannel.send(it) }
            }.launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun deleteNote() {
        action<ListNoteAction.DeleteNote>()
            .flatMapLatest {
                flow {
                    fileUseCase.deleteDirectory(it.context, it.noteModel.nameMediaNote)
                    noteUseCase.deleteNote(it.noteModel).fold(
                        success = {
                            ResultContent.Content(it)
                        },
                        failure = {
                            ResultContent.Error(it)
                        }
                    ).let { emit(it) }
                }
            }.onEach { result ->
                val event = when (result) {
                    is ResultContent.Loading -> null
                    is ResultContent.Content -> ListNoteSingleEvent.DeleteNoteSuccess
                    is ResultContent.Error -> ListNoteSingleEvent.SingleEventFailed(error = result.error)
                }
                event?.let { _singleEventChannel.send(it) }
            }.launchIn(viewModelScope)
    }

    private companion object {
        private const val STATE_KEY = "ListNoteViewModel.state"
    }
}