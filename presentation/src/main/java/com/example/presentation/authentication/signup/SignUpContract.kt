package com.example.presentation.authentication.signup

import android.os.Parcelable
import android.util.Patterns
import kotlinx.parcelize.Parcelize

sealed interface SignUpAction {
    data class EmailChanged(val email: String, val isActiveButton: Boolean = true) : SignUpAction
    data class PasswordChanged(val password: String, val isActiveButton: Boolean = true) :
        SignUpAction

    data class PasswordConfirmChanged(val password: String, val isActiveButton: Boolean = true) :
        SignUpAction

    object SignUp : SignUpAction

    @JvmInline
    value class EmailFocusChanged(val force: Boolean = true) : SignUpAction

    @JvmInline
    value class PasswordFocusChanged(val force: Boolean = true) : SignUpAction

    @JvmInline
    value class PasswordConfirmFocusChanged(val force: Boolean = true) : SignUpAction
}

sealed interface SignUpSingleEvent {
    sealed interface SignUpUser : SignUpSingleEvent {
        object Success : SignUpUser
        data class Failed(val error: Throwable) : SignUpUser
    }

    sealed interface ValidationError : SignUpSingleEvent {
        @JvmInline
        value class Email(val validationErrors: () -> Set<SignUpUiState.SignUpValidationError>) :
            ValidationError

        @JvmInline
        value class Password(val validationErrors: () -> Set<SignUpUiState.SignUpValidationError>) :
            ValidationError

        @JvmInline
        value class PasswordConfirm(val validationErrors: () -> Set<SignUpUiState.SignUpValidationError>) :
            ValidationError
    }
}

@Parcelize
data class SignUpUiState(
    val isLoading: Boolean,
    val email: String?,
    val password: String?,
    val passwordConfirm: String?,
    val validationErrors: Set<SignUpValidationError>,
    val isActiveButton: Boolean
) : Parcelable {
    companion object {
        val INITIAL = SignUpUiState(
            isLoading = false,
            email = null,
            password = null,
            passwordConfirm = null,
            validationErrors = SignUpValidationError.values().toSet(),
            isActiveButton = false
        )
    }

    enum class SignUpValidationError {
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

internal fun buildSignUpUiState(
    email: EmailValidationResult,
    password: PasswordValidationResult,
    passwordConfirm: PasswordValidationResult,
    isActiveButton: Boolean,
    isLoading: Boolean
): SignUpUiState =
    SignUpUiState(
        isLoading = isLoading,
        isActiveButton = isActiveButton,
        email = email.value,
        password = password.value,
        passwordConfirm = passwordConfirm.value,
        validationErrors = buildSet(capacity = 3) {
            email.error?.let { add(it) }
            password.error?.let { add(it) }
            passwordConfirm.error?.let { add(it) }
        }
    )

// -------------------------------------------- VALIDATIONS --------------------------------------------

internal data class EmailValidationResult(
    val value: String,
    val error: SignUpUiState.SignUpValidationError?
)

internal data class PasswordValidationResult(
    val value: String,
    val error: SignUpUiState.SignUpValidationError?
)

const val EMAIL_MAX_LENGTH = 90
internal fun validateEmail(s: String): EmailValidationResult = EmailValidationResult(
    value = s,
    error = when {
        s.isEmpty() -> SignUpUiState.SignUpValidationError.EMPTY_EMAIL
        s.length > EMAIL_MAX_LENGTH -> SignUpUiState.SignUpValidationError.INVALID_LENGTH_EMAIL
        !(Patterns.EMAIL_ADDRESS.matcher(s).matches()) ->
            SignUpUiState.SignUpValidationError.INVALID_FORMAT_EMAIL

        else -> null
    }
)

const val PASSWORD_MIN_LENGTH = 6
internal fun validatePassword(s: String): PasswordValidationResult = PasswordValidationResult(
    value = s,
    error = when {
        s.isEmpty() -> SignUpUiState.SignUpValidationError.EMPTY_PASSWORD
        s.length < PASSWORD_MIN_LENGTH -> SignUpUiState.SignUpValidationError.INVALID_LENGTH_PASSWORD
        else -> null
    }
)

internal fun validatePasswordConfirm(
    password: String,
    passwordConfirm: String,
): PasswordValidationResult =
    PasswordValidationResult(
        value = password,
        error = if (password == passwordConfirm) {
            null
        } else {
            when {
                passwordConfirm.isEmpty() -> SignUpUiState.SignUpValidationError.EMPTY_PASSWORD_CONFIRM
                passwordConfirm.length < PASSWORD_MIN_LENGTH -> SignUpUiState.SignUpValidationError.INVALID_LENGTH_PASSWORD_CONFIRM
                else -> SignUpUiState.SignUpValidationError.NOT_MATCH_PASSWORD_CONFIRM
            }
        }
    )