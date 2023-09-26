package com.example.presentation.authentication.information

import androidx.lifecycle.viewModelScope
import com.example.core.base.BaseViewModel
import com.example.core.core.external.ResultContent
import com.example.domain.usecase.data.FirebaseStorageUseCase
import com.example.domain.usecase.data.UserUseCase
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
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserInformationViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val firebaseStorageUseCase: FirebaseStorageUseCase
) : BaseViewModel() {
    private val _actionSharedFlow =
        MutableSharedFlow<UserInformationAction>(extraBufferCapacity = 64)

    private inline fun <reified T : UserInformationAction> action() =
        _actionSharedFlow.filterIsInstance<T>()

    private val _singleEventChannel =
        Channel<UserInformationSingleEvent>(Channel.UNLIMITED).addToBag()
    val singleEventFlow: Flow<UserInformationSingleEvent> get() = _singleEventChannel.receiveAsFlow()

    fun dispatch(action: UserInformationAction) =
        viewModelScope.launch { _actionSharedFlow.emit(action) }

    init {
        deleteUser()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun deleteUser() {
        action<UserInformationAction.DeleteUser>()
            .flatMapLatest {
                flow {
                    emit(ResultContent.Loading)
                    firebaseStorageUseCase.deleteAllDirectory()
                        .fold(
                            success = {
                                ResultContent.Content(it)
                            },
                            failure = {
                                ResultContent.Error(it)
                            }
                        ).let { emit(it) }
                }.zip(
                    flow {
                        emit(ResultContent.Loading)
                        userUseCase.deleteUser()
                            .fold(
                                success = {
                                    ResultContent.Content(it)
                                },
                                failure = {
                                    ResultContent.Error(it)
                                }
                            ).let { emit(it) }
                    }
                ) { result, _ ->
                    result
                }
            }.onEach { lce ->
                val event = when (lce) {
                    is ResultContent.Loading -> null
                    is ResultContent.Content -> UserInformationSingleEvent.DeleteUser.Success
                    is ResultContent.Error -> UserInformationSingleEvent.DeleteUser.Failed(error = lce.error)
                }
                event?.let { _singleEventChannel.send(it) }
            }.launchIn(viewModelScope)
    }
}