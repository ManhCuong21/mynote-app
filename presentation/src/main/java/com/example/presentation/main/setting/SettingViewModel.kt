package com.example.presentation.main.setting

import androidx.lifecycle.viewModelScope
import com.example.core.base.BaseViewModel
import com.example.core.core.external.ResultContent
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val userUseCase: UserUseCase
) : BaseViewModel() {
    private val _actionSharedFlow = MutableSharedFlow<SettingAction>(extraBufferCapacity = 64)
    private inline fun <reified T : SettingAction> action() =
        _actionSharedFlow.filterIsInstance<T>()

    private val _singleEventChannel = Channel<SettingSingleEvent>(Channel.UNLIMITED).addToBag()
    val singleEventFlow: Flow<SettingSingleEvent> get() = _singleEventChannel.receiveAsFlow()

    fun dispatch(action: SettingAction) =
        viewModelScope.launch { _actionSharedFlow.emit(action) }

    init {
        signOut()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun signOut() {
        action<SettingAction.SignOut>()
            .flatMapLatest {
                flow {
                    emit(ResultContent.Loading)
                    userUseCase.signOutUser()
                        .fold(
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
                    is ResultContent.Content -> SettingSingleEvent.SignOutUser.Success
                    is ResultContent.Error -> SettingSingleEvent.SignOutUser.Failed(error = lce.error)
                }
                event?.let { _singleEventChannel.send(it) }
            }.launchIn(viewModelScope)
    }
}