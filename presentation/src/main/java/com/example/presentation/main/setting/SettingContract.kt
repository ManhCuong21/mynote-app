package com.example.presentation.main.setting


sealed interface SettingAction {
    object SignOut : SettingAction
}

sealed interface SettingSingleEvent {
    sealed interface SignOutUser : SettingSingleEvent {
        object Success : SignOutUser
        data class Failed(val error: Throwable) : SignOutUser
    }
}