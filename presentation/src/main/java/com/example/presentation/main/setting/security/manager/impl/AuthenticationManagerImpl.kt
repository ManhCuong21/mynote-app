package com.example.presentation.main.setting.security.manager.impl

import com.example.core.core.sharepref.SharedPrefersManager
import com.example.presentation.main.setting.security.manager.AuthMethod
import com.example.presentation.main.setting.security.manager.AuthenticationManager
import com.example.presentation.navigation.MainNavigator
import javax.inject.Inject

class AuthenticationManagerImpl @Inject constructor(
    private val mainNavigator: MainNavigator,
    private val sharedPrefersManager: SharedPrefersManager
) : AuthenticationManager {
    override fun openAuthentication() {
        val authMethod = sharedPrefersManager.authMethod
        when (authMethod) {
            AuthMethod.PASSWORD.name -> {
            }

            AuthMethod.PIN.name -> {
                mainNavigator.navigate(MainNavigator.Direction.SecurityFragmentToSetupUnlockCodeFragment(AuthMethod.PIN))
            }

            AuthMethod.FINGERPRINT.name -> {

            }
        }
    }
}