package com.example.presentation.dialog.biometric

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
import javax.inject.Inject

class BiometricAuthenticationManagerImpl @Inject constructor(
    private val activity: Activity
) : com.example.presentation.dialog.biometric.BiometricManager {

    private val fragmentActivity: FragmentActivity by lazy { activity as FragmentActivity }
    private val executor = ContextCompat.getMainExecutor(fragmentActivity)
    private val biometricManager by lazy { BiometricManager.from(fragmentActivity) }

    override fun verifyBiometric(onSucceeded: () -> Unit, onFailed: () -> Unit) {
        when (biometricManager.canAuthenticate(AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometric(onSucceeded, onFailed)
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                handleEnrollment()
            }

            else -> {
                Toast.makeText(
                    activity,
                    "Biometric features are currently unavailable",
                    Toast.LENGTH_SHORT
                ).show()
                onFailed()
            }
        }
    }

    override fun showBiometric(onSucceeded: () -> Unit, onFailed: () -> Unit) {
        if (fragmentActivity.supportFragmentManager.isStateSaved) return

        val biometricPrompt = BiometricPrompt(
            fragmentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onFailed()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSucceeded()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .setTitle("Biometric login")
            .setSubtitle("Log in using your biometric credential")
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (_: Exception) {
            onFailed()
        }
    }

    private fun handleEnrollment() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, AUTHENTICATORS)
            }
            activity.startActivity(enrollIntent)
        } else {
            activity.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
        }
    }

    companion object {
        private const val AUTHENTICATORS = BIOMETRIC_STRONG or DEVICE_CREDENTIAL
    }
}