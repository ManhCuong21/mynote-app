package com.example.presentation.authentication.signin

import android.os.Parcelable
import android.util.Patterns
import kotlinx.parcelize.Parcelize

sealed interface SignInAction {
    data class EmailChanged(val email: String, val isActiveButton: Boolean = true) : SignInAction
    data class PasswordChanged(val password: String, val isActiveButton: Boolean = true) :
        SignInAction

    object SignIn : SignInAction

    @JvmInline
    value class EmailFocusChanged(val force: Boolean = true) : SignInAction

    @JvmInline
    value class PasswordFocusChanged(val force: Boolean = true) : SignInAction
}

sealed interface SignInSingleEvent {
    sealed interface SignInUser : SignInSingleEvent {
        object Success : SignInUser
        data class Failed(val error: Throwable) : SignInUser
    }

    sealed interface ValidationError : SignInSingleEvent {
        @JvmInline
        value class Email(val validationErrors: () -> Set<SignInUiState.SignInValidationError>) :
            ValidationError

        @JvmInline
        value class Password(val validationErrors: () -> Set<SignInUiState.SignInValidationError>) :
            ValidationError
    }
}

@Parcelize
data class SignInUiState(
    val isLoading: Boolean,
    val email: String?,
    val password: String?,
    val validationErrors: Set<SignInValidationError>,
    val isActiveButton: Boolean
) : Parcelable {
    companion object {
        val INITIAL = SignInUiState(
            isLoading = false,
            email = null,
            password = null,
            validationErrors = SignInValidationError.values().toSet(),
            isActiveButton = false
        )
    }

    enum class SignInValidationError {
        EMPTY_EMAIL,
        INVALID_LENGTH_EMAIL,
        INVALID_FORMAT_EMAIL,
        EMPTY_PASSWORD,
        INVALID_LENGTH_PASSWORD,
        EMPTY_PASSWORD_CONFIRM,
        INVALID_LENGTH_PASSWORD_CONFIRM,
        NOT_MATCH_PASSWORD_CONFIRM
    }
}

internal fun buildSignInUiState(
    email: EmailValidationResult,
    password: PasswordValidationResult,
    isActiveButton: Boolean,
    isLoading: Boolean
): SignInUiState =
    SignInUiState(
        isLoading = isLoading,
        isActiveButton = isActiveButton,
        email = email.value,
        password = password.value,
        validationErrors = buildSet(capacity = 2) {
            email.error?.let { add(it) }
            password.error?.let { add(it) }
        }
    )

// -------------------------------------------- VALIDATIONS --------------------------------------------

internal data class EmailValidationResult(
    val value: String,
    val error: SignInUiState.SignInValidationError?
)

internal data class PasswordValidationResult(
    val value: String,
    val error: SignInUiState.SignInValidationError?
)

const val EMAIL_MAX_LENGTH = 90
internal fun validateEmail(s: String): EmailValidationResult = EmailValidationResult(
    value = s,
    error = when {
        s.isEmpty() -> SignInUiState.SignInValidationError.EMPTY_EMAIL
        s.length > EMAIL_MAX_LENGTH -> SignInUiState.SignInValidationError.INVALID_LENGTH_EMAIL
        !(Patterns.EMAIL_ADDRESS.matcher(s).matches()) ->
            SignInUiState.SignInValidationError.INVALID_FORMAT_EMAIL

        else -> null
    }
)

const val PASSWORD_MIN_LENGTH = 6
internal fun validatePassword(s: String): PasswordValidationResult = PasswordValidationResult(
    value = s,
    error = when {
        s.isEmpty() -> SignInUiState.SignInValidationError.EMPTY_PASSWORD
        s.length < PASSWORD_MIN_LENGTH -> SignInUiState.SignInValidationError.INVALID_LENGTH_PASSWORD
        else -> null
    }
)