package com.example.presentation.authentication.biometric

import androidx.fragment.app.FragmentActivity

interface BiometricAuthenticationManager {
    fun verifyBiometric(activity: FragmentActivity, onSucceeded: () -> Unit, onFailed: () -> Unit)
    fun showBiometric(activity: FragmentActivity, onSucceeded: () -> Unit, onFailed: () -> Unit)
}

enum class AuthMethod {
    PASSWORD, PIN
}