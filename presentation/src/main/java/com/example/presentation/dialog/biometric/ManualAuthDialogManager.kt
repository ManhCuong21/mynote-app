package com.example.presentation.dialog.biometric

interface ManualAuthDialogManager {
    fun showManualAuth(
        title: String,
        onSuccess: () -> Unit
    )
}