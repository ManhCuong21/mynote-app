package com.example.presentation.main.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.base.BaseViewModel
import com.example.core.core.external.ResultContent
import com.example.core.core.model.CategoryModel
import com.example.domain.usecase.data.CategoryUseCase
import com.example.presentation.R
import com.github.michaelbull.result.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val categoryUseCase: CategoryUseCase
) : BaseViewModel() {

    private val _actionSharedFlow = MutableSharedFlow<HomeAction>(extraBufferCapacity = 64)
    private inline fun <reified T : HomeAction> action() = _actionSharedFlow.filterIsInstance<T>()

    private val _singleEventChannel = Channel<HomeSingleEvent>(Channel.UNLIMITED).addToBag()
    val singleEventFlow = _singleEventChannel.receiveAsFlow()

    val stateFlow: StateFlow<HomeUiState>

    init {
        val initial = savedStateHandle.get<HomeUiState>(STATE_KEY) ?: HomeUiState.INITIAL

        val listCategory = action<HomeAction.ListCategoryChanged>()
            .map { it.list }
            .onStart { emit(initial.listCategory) }
            .distinctUntilChanged()

        val listNote = action<HomeAction.ListNoteChanged>()
            .map { it.list }
            .onStart { emit(initial.listNote) }
            .distinctUntilChanged()

        stateFlow = combine(listCategory, listNote, ::buildHomeUiState)
            .onEach { savedStateHandle[STATE_KEY] = it }
            .stateIn(viewModelScope, SharingStarted.Eagerly, initial)

        observeGetListCategory()
        dispatch(HomeAction.GetListCategory)
    }

    fun dispatch(action: HomeAction) {
        viewModelScope.launch { _actionSharedFlow.emit(action) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeGetListCategory() {
        action<HomeAction.GetListCategory>()
            .flatMapLatest {
                flow {
                    emit(ResultContent.Loading)
                    val result = categoryUseCase.readAllCategory().fold(
                        success = { ResultContent.Content(it) },
                        failure = { ResultContent.Error(it) }
                    )
                    emit(result)
                }
            }
            .onEach { result ->
                val event = when (result) {
                    is ResultContent.Content -> {
                        val allCategory = CategoryModel(-1, "All", R.drawable.icon_clock.toString(), false)
                        HomeSingleEvent.GetListCategory.Success(listOf(allCategory) + result.content)
                    }
                    is ResultContent.Error -> HomeSingleEvent.GetListCategory.Failed(result.error)
                    else -> null
                }
                event?.let { _singleEventChannel.send(it) }
            }
            .launchIn(viewModelScope)
    }

    private companion object {
        private const val STATE_KEY = "HomeViewModel.state"
    }
}