package com.example.presentation.main.setting.security.manager

interface AuthenticationManager {
    fun openAuthentication()
}

enum class AuthMethod {
    PASSWORD, PIN, FINGERPRINT
}