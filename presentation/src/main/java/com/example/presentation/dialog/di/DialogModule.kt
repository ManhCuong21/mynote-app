package com.example.presentation.dialog.di

import com.example.presentation.dialog.biometric.BiometricAuthenticationManagerImpl
import com.example.presentation.dialog.biometric.BiometricManager
import com.example.presentation.dialog.biometric.ManualAuthDialogManager
import com.example.presentation.dialog.biometric.ManualAuthDialogManagerImpl
import com.example.presentation.dialog.permission.PermissionManager
import com.example.presentation.dialog.permission.PermissionManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
abstract class DialogModule {

    @Binds
    @ActivityScoped
    abstract fun biometricAuthenticationManager(impl: BiometricAuthenticationManagerImpl): BiometricManager

    @Binds
    @ActivityScoped
    abstract fun bindManualAuthDialogManager(impl: ManualAuthDialogManagerImpl): ManualAuthDialogManager

    @Binds
    @ActivityScoped
    abstract fun bindPermissionManager(impl: PermissionManagerImpl): PermissionManager
}