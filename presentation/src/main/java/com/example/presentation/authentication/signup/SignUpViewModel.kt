package com.example.presentation.authentication.signup

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userUseCase: UserUseCase
) : BaseViewModel() {
    private val _mutableStateFlow: MutableStateFlow<SignUpUiState>
    val stateFlow: StateFlow<SignUpUiState>
    private val loadingStateFlow = MutableStateFlow(false)

    private val _actionSharedFlow = MutableSharedFlow<SignUpAction>(extraBufferCapacity = 64)
    private inline fun <reified T : SignUpAction> action() =
        _actionSharedFlow.filterIsInstance<T>()

    private val _singleEventChannel = Channel<SignUpSingleEvent>(Channel.UNLIMITED).addToBag()
    val singleEventFlow: Flow<SignUpSingleEvent> get() = _singleEventChannel.receiveAsFlow()

    fun dispatch(action: SignUpAction) =
        viewModelScope.launch { _actionSharedFlow.emit(action) }

    init {
        val initialUiState =
            savedStateHandle.get<SignUpUiState?>(STATE_KEY)?.copy(isLoading = false)
                ?: SignUpUiState.INITIAL
        _mutableStateFlow = MutableStateFlow(initialUiState).apply {
            onEach { savedStateHandle[STATE_KEY] = it }.launchIn(viewModelScope)
        }
        val activeButtonFlow = MutableStateFlow(false)

        val gmailFlow = action<SignUpAction.EmailChanged>()
            .map {
                activeButtonFlow.value = it.isActiveButton
                it.email
            }
            .onStart { emit(initialUiState.email.orEmpty()) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val passwordFlow = action<SignUpAction.PasswordChanged>()
            .map {
                activeButtonFlow.value = it.isActiveButton
                it.password
            }
            .onStart { emit(initialUiState.password.orEmpty()) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val passwordConfirmFlow = action<SignUpAction.PasswordConfirmChanged>()
            .map {
                activeButtonFlow.value = it.isActiveButton
                it.password
            }
            .onStart { emit(initialUiState.passwordConfirm.orEmpty()) }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val gmail = gmailFlow
            .map(::validateEmail)
            .distinctUntilChanged()
        val password = passwordFlow
            .map(::validatePassword)
            .distinctUntilChanged()
        val passwordConfirm = combine(
            passwordFlow,
            passwordConfirmFlow,
            ::validatePasswordConfirm
        )
            .distinctUntilChanged()

        stateFlow = combine(
            gmail,
            password,
            passwordConfirm,
            activeButtonFlow,
            loadingStateFlow,
            ::buildSignUpUiState
        ).onEach {
            savedStateHandle[STATE_KEY] = it
        }.stateIn(viewModelScope, SharingStarted.Eagerly, initialUiState)

        setupFocusChangedActions()
        signUpSubmit()
    }

    private fun setupFocusChangedActions() {
        val validationErrorsAccessor = { stateFlow.value.validationErrors }
        merge(
            action<SignUpAction.EmailFocusChanged>()
                .filter { it.force }
                .map { SignUpSingleEvent.ValidationError.Email(validationErrorsAccessor) },
            action<SignUpAction.PasswordFocusChanged>()
                .filter { it.force }
                .map { SignUpSingleEvent.ValidationError.Password(validationErrorsAccessor) },
            action<SignUpAction.PasswordConfirmFocusChanged>()
                .filter { it.force }
                .map { SignUpSingleEvent.ValidationError.PasswordConfirm(validationErrorsAccessor) }
        )
            .onEach { _singleEventChannel.send(it) }
            .launchIn(viewModelScope)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun signUpSubmit() {
        action<SignUpAction.SignUp>()
            .flatMapLatest {
                flow {
                    emit(ResultContent.Loading)
                    userUseCase.signUpUser(
                        email = stateFlow.value.email.orEmpty(),
                        password = stateFlow.value.password.orEmpty()
                    ).fold(
                        success = {
                            ResultContent.Content(it)
                        },
                        failure = {
                            ResultContent.Error(it)
                        }
                    ).let { emit(it) }
                }
            }.onEach { result ->
                _mutableStateFlow.update { state ->
                    state.copy(isLoading = result is ResultContent.Loading)
                }
                val event = when (result) {
                    is ResultContent.Loading -> null
                    is ResultContent.Content -> SignUpSingleEvent.SignUpUser.Success
                    is ResultContent.Error -> SignUpSingleEvent.SignUpUser.Failed(error = result.error)
                }
                event?.let { _singleEventChannel.send(it) }
                loadingStateFlow.value = result === ResultContent.Loading
            }.launchIn(viewModelScope)
    }

    private companion object {
        private const val STATE_KEY = "SignUpViewModel.state"
    }
}