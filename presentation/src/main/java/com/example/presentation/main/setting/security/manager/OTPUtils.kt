package com.example.presentation.main.setting.security.manager

class OTPUtils {
    // Khai báo hàm native
    external fun encryptOTP(otp: String, secretKey: String): String
    external fun decryptOTP(encryptedOTP: String, secretKey: String): String

    companion object {
        init {
            // Nạp thư viện C++ (tên thư viện mà bạn đã biên dịch)
            System.loadLibrary("otp")
        }
    }
}
