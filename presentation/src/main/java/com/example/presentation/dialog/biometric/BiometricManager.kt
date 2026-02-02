package com.example.presentation.dialog.biometric

interface BiometricManager {
    fun verifyBiometric(onSucceeded: () -> Unit, onFailed: () -> Unit)
    fun showBiometric(onSucceeded: () -> Unit, onFailed: () -> Unit)
}