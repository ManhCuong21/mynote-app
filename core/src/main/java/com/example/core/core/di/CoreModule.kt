package com.example.core.core.di

import com.example.core.core.external.AppCoroutineDispatchers
import com.example.core.core.external.DefaultAppCoroutineDispatchers
import com.example.core.core.file.FileExtension
import com.example.core.core.file.FileExtensionImpl
import com.example.core.core.file.image.ImageFile
import com.example.core.core.file.image.ImageFileImpl
import com.example.core.core.file.record.RecordFile
import com.example.core.core.file.record.RecordFileImpl
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

    @Binds
    fun imageFile(impl: ImageFileImpl): ImageFile

    @Binds
    fun recordFile(impl: RecordFileImpl): RecordFile
}