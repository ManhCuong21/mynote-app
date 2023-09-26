package com.example.presentation.authentication.information

sealed interface UserInformationAction {
    object DeleteUser : UserInformationAction
}

sealed interface UserInformationSingleEvent {
    sealed interface DeleteUser : UserInformationSingleEvent {
        object Success : DeleteUser
        data class Failed(val error: Throwable) : DeleteUser
    }
}