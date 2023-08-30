package com.example.presentation.main.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.base.BaseViewModel
import com.example.core.core.external.ResultContent
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
class HomeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val categoryUseCase: com.example.domain.usecase.CategoryUseCase
) : BaseViewModel() {
    private val _mutableStateFlow: MutableStateFlow<HomeUiState>
    val stateFlow: StateFlow<HomeUiState>

    private val _actionSharedFlow = MutableSharedFlow<HomeAction>(extraBufferCapacity = 64)
    private inline fun <reified T : HomeAction> action() =
        _actionSharedFlow.filterIsInstance<T>()

    private val _singleEventChannel = Channel<HomeSingleEvent>(Channel.UNLIMITED).addToBag()
    val singleEventFlow: Flow<HomeSingleEvent> get() = _singleEventChannel.receiveAsFlow()

    fun dispatch(action: HomeAction) =
        viewModelScope.launch { _actionSharedFlow.emit(action) }

    init {
        val initialUiState = savedStateHandle.get<HomeUiState?>(STATE_KEY)?.copy()
            ?: HomeUiState.INITIAL
        _mutableStateFlow = MutableStateFlow(initialUiState).apply {
            onEach { savedStateHandle[STATE_KEY] = it }.launchIn(viewModelScope)
        }
        val listCategoryFlow = action<HomeAction.ListCategoryChanged>()
            .map {
                it.list
            }
            .onStart { emit(initialUiState.listCategory) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val listNoteFlow = action<HomeAction.ListNoteChanged>()
            .map {
                it.list
            }
            .onStart { emit(initialUiState.listNote) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val listCategory = listCategoryFlow.distinctUntilChanged()
        val listNote = listNoteFlow.distinctUntilChanged()

        stateFlow = combine(
            listCategory,
            listNote,
            ::buildHomeUiState
        ).onEach {
            savedStateHandle[STATE_KEY] = it
        }.stateIn(viewModelScope, SharingStarted.Eagerly, initialUiState)
        getListCategory()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getListCategory() {
        action<HomeAction.GetListCategory>()
            .flatMapLatest {
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
                }
            }.onEach { result ->
                val event = when (result) {
                    is ResultContent.Loading -> null
                    is ResultContent.Content -> HomeSingleEvent.GetListCategory.Success(result.content)
                    is ResultContent.Error -> HomeSingleEvent.GetListCategory.Failed(error = result.error)
                }
                event?.let { _singleEventChannel.send(it) }
            }.launchIn(viewModelScope)
    }

    private companion object {
        private const val STATE_KEY = "HomeViewModel.state"
    }
}