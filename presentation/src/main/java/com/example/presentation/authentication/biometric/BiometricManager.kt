package com.example.presentation.authentication.biometric

import androidx.fragment.app.FragmentActivity

interface BiometricAuthenticationManager {
    fun verifyBiometricAvailable(requestCode: Int)
    fun showBiometric(activity: FragmentActivity, onSucceeded: () -> Unit, onFailed: () -> Unit)
}