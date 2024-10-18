package com.example.presentation.navigation

import com.example.presentation.authentication.biometric.BiometricAuthenticationManager
import com.example.presentation.authentication.biometric.BiometricAuthenticationManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
internal interface NavigatorActivityModule {
    @Binds
    @ActivityScoped
    fun mainNavigator(impl: MainNavigatorImpl): MainNavigator

    @Binds
    @ActivityScoped
    fun biometricAuthenticationManager(impl: BiometricAuthenticationManagerImpl): BiometricAuthenticationManager
}