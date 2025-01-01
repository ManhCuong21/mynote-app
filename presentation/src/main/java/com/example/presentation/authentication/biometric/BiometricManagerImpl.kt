package com.example.presentation.authentication.biometric

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor
import javax.inject.Inject

class BiometricAuthenticationManagerImpl @Inject constructor() : BiometricAuthenticationManager {
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun verifyBiometric(
        activity: FragmentActivity,
        onSucceeded: () -> Unit,
        onFailed: () -> Unit
    ) {
        val biometricManager = BiometricManager.from(activity)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> showBiometric(activity, onSucceeded, onFailed)

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Toast.makeText(
                    activity, "No biometric features available on this device", Toast.LENGTH_SHORT
                ).show()

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Toast.makeText(
                    activity, "Biometric features are currently unavailable", Toast.LENGTH_SHORT
                ).show()

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                val enrollIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                        )
                    }
                } else {
                    TODO("VERSION.SDK_INT < R")
                }
                activity.startActivityForResult(enrollIntent, 1)
            }

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {}

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {}

            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {}
        }
    }

    override fun showBiometric(
        activity: FragmentActivity,
        onSucceeded: () -> Unit,
        onFailed: () -> Unit
    ) {
        executor = ContextCompat.getMainExecutor(activity)
        biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    onFailed()
                    Toast.makeText(
                        activity, "Authentication error: $errString", Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    onSucceeded()
                    Toast.makeText(
                        activity,
                        "Authentication succeeded!", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                    Toast.makeText(activity, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}