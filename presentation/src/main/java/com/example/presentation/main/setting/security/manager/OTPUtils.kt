package com.example.presentation.main.setting.security.manager

class OTPUtils {
    external fun encryptOTP(otp: String, secretKey: String): String
    external fun decryptOTP(encryptedOTP: String, secretKey: String): String

    companion object {
        init {
            System.loadLibrary("otp")
        }
    }
}