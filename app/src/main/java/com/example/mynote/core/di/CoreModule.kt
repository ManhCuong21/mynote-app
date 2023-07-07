package com.example.mynote.core.di

import com.example.mynote.core.external.AppCoroutineDispatchers
import com.example.mynote.core.external.DefaultAppCoroutineDispatchers
import com.example.mynote.core.external.FileExtension
import com.example.mynote.core.external.FileExtensionImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
internal interface CoreModule {
    @Binds
    @Singleton
    fun appCoroutineDispatchers(impl: DefaultAppCoroutineDispatchers): AppCoroutineDispatchers

    @Binds
    fun fileExtension(impl: FileExtensionImpl): FileExtension
}