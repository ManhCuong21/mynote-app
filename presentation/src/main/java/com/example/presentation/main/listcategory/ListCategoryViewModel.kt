package com.example.presentation.main.listcategory

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
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListCategoryViewModel @Inject constructor(
    private val categoryUseCase: CategoryUseCase
) : BaseViewModel() {
    private val _actionSharedFlow = MutableSharedFlow<ListCategoryAction>(extraBufferCapacity = 64)
    private val _singleEventChannel = Channel<ListCategorySingleEvent>(Channel.UNLIMITED).addToBag()
    val singleEventFlow: Flow<ListCategorySingleEvent> get() = _singleEventChannel.receiveAsFlow()
    private inline fun <reified T : ListCategoryAction> action() =
        _actionSharedFlow.filterIsInstance<T>()

    fun dispatch(action: ListCategoryAction) =
        viewModelScope.launch { _actionSharedFlow.emit(action) }

    init {
        getListCategory()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getListCategory() {
        action<ListCategoryAction.GetListCategory>()
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
                    is ResultContent.Content -> ListCategorySingleEvent.GetListCategory.Success(
                        result.content
                    )

                    is ResultContent.Error -> ListCategorySingleEvent.GetListCategory.Failed(error = result.error)
                }
                event?.let { _singleEventChannel.send(it) }
            }.launchIn(viewModelScope)
    }
}