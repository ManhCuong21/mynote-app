package com.example.mynote.core.di

import com.example.mynote.core.external.AppCoroutineDispatchers
import com.example.mynote.core.external.DefaultAppCoroutineDispatchers
import com.example.mynote.core.file.FileExtension
import com.example.mynote.core.file.FileExtensionImpl
import com.example.mynote.core.file.image.ImageFile
import com.example.mynote.core.file.image.ImageFileImpl
import com.example.mynote.core.file.record.RecordFile
import com.example.mynote.core.file.record.RecordFileImpl
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