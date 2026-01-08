package com.example.presentation.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.base.BaseViewModel
import com.example.core.core.external.ResultContent
import com.example.core.core.model.CategoryModel
import com.example.domain.mapper.CategoryParams
import com.example.domain.usecase.data.CategoryUseCase
import com.github.michaelbull.result.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
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
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val categoryUseCase: CategoryUseCase
) : BaseViewModel() {
    val uiStateFlow: StateFlow<CategoryUiState>

    private val _actionSharedFlow = MutableSharedFlow<CategoryAction>(extraBufferCapacity = 64)
    private inline fun <reified T : CategoryAction> action() =
        _actionSharedFlow.filterIsInstance<T>()

    private val _singleEventChannel = Channel<AddCategorySingleEvent>(Channel.UNLIMITED).addToBag()
    val singleEventFlow: Flow<AddCategorySingleEvent> get() = _singleEventChannel.receiveAsFlow()

    fun dispatch(action: CategoryAction) =
        viewModelScope.launch { _actionSharedFlow.emit(action) }

    init {
        val initialUiState = savedStateHandle.get<CategoryUiState?>(STATE_KEY)?.copy()
            ?: CategoryUiState.INITIAL

        insertCategory()
        updateCategory()

        val categoryTitleFlow = action<CategoryAction.TitleCategoryChanged>()
            .map { it.title }
            .onStart { emit(initialUiState.title) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val imageFlow = action<CategoryAction.ImageCategoryChanged>()
            .map { it.image }
            .onStart { emit(initialUiState.image) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val securityFlow = action<CategoryAction.SecurityCategoryChanged>()
            .map { it.securityCategory }
            .onStart { emit(initialUiState.securityCategory) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        uiStateFlow = combine(
            categoryTitleFlow.distinctUntilChanged(),
            imageFlow.distinctUntilChanged(),
            securityFlow.distinctUntilChanged(),
            ::buildCategoryUiState
        ).onEach {
            savedStateHandle[STATE_KEY] = it
        }.stateIn(viewModelScope, SharingStarted.Eagerly, initialUiState)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun insertCategory() {
        action<CategoryAction.InsertCategory>()
            .flatMapLatest {
                flow {
                    emit(ResultContent.Loading)
                    categoryUseCase.insertCategory(
                        CategoryParams(
                            title = uiStateFlow.value.title,
                            image = uiStateFlow.value.image,
                            security = uiStateFlow.value.securityCategory
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun updateCategory() {
        action<CategoryAction.UpdateCategory>()
            .flatMapLatest {
                flow {
                    emit(ResultContent.Loading)
                    categoryUseCase.updateCategory(
                        CategoryModel(
                            idCategory = it.categoryModel.idCategory,
                            titleCategory = uiStateFlow.value.title,
                            imageCategory = uiStateFlow.value.image,
                            securityCategory = uiStateFlow.value.securityCategory
                        )
                    ).fold(
                        success = { content ->
                            ResultContent.Content(content)
                        },
                        failure = { error ->
                            ResultContent.Error(error)
                        }
                    ).let { value -> emit(value) }
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