package com.example.presentation.navigation

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
}
