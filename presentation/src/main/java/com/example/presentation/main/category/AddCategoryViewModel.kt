package com.example.presentation.main.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.base.BaseViewModel
import com.example.core.core.external.ResultContent
import com.example.domain.usecase.CategoryUseCase
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
class AddCategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val categoryUseCase: CategoryUseCase
) : BaseViewModel() {
    private val _mutableStateFlow: MutableStateFlow<AddCategoryUiState>
    val stateFlow: StateFlow<AddCategoryUiState>

    private val _actionSharedFlow = MutableSharedFlow<AddCategoryAction>(extraBufferCapacity = 64)
    private inline fun <reified T : AddCategoryAction> action() =
        _actionSharedFlow.filterIsInstance<T>()

    private val _singleEventChannel = Channel<AddCategorySingleEvent>(Channel.UNLIMITED).addToBag()
    val singleEventFlow: Flow<AddCategorySingleEvent> get() = _singleEventChannel.receiveAsFlow()

    fun dispatch(action: AddCategoryAction) =
        viewModelScope.launch { _actionSharedFlow.emit(action) }

    init {
        val initialUiState =
            savedStateHandle.get<AddCategoryUiState?>(STATE_KEY)?.copy()
                ?: AddCategoryUiState.INITIAL
        _mutableStateFlow = MutableStateFlow(initialUiState).apply {
            onEach { savedStateHandle[STATE_KEY] = it }.launchIn(viewModelScope)
        }

        saveCategory()

        val categoryTitleFlow = action<AddCategoryAction.TitleCategoryChanged>()
            .map {
                it.value
            }
            .onStart { emit(initialUiState.title) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val imageFlow = action<AddCategoryAction.ImageCategoryChanged>()
            .map {
                it.value
            }.onStart { initialUiState.image }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val title = categoryTitleFlow.distinctUntilChanged()
        val image = imageFlow.distinctUntilChanged()

        stateFlow = combine(
            title,
            image,
            ::buildAddCategoryUiState
        ).onEach {
            savedStateHandle[STATE_KEY] = it
        }.stateIn(viewModelScope, SharingStarted.Eagerly, initialUiState)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun saveCategory() {
        action<AddCategoryAction.SaveCategory>()
            .flatMapLatest {
                flow {
                    emit(ResultContent.Loading)
                    categoryUseCase.insertCategory(
                        com.example.domain.model.CategoryModel(
                            title = stateFlow.value.title,
                            image = stateFlow.value.image
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
                    is ResultContent.Content -> AddCategorySingleEvent.SaveCategory.Success
                    is ResultContent.Error -> AddCategorySingleEvent.SaveCategory.Failed(error = lce.error)
                }
                event?.let { _singleEventChannel.send(it) }
            }.launchIn(viewModelScope)
    }

    private companion object {
        private const val STATE_KEY = "CategoryViewModel.state"
    }
}