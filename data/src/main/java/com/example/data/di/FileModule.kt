package com.example.data.di

import com.example.data.file.FileRepository
import com.example.data.file.FileRepositoryImpl
import com.example.data.file.image.ImageFileRepository
import com.example.data.file.image.ImageFileRepositoryImpl
import com.example.data.file.record.RecordFileRepository
import com.example.data.file.record.RecordFileRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface FileModule {

    @Binds
    fun fileExtension(impl: FileRepositoryImpl): FileRepository

    @Binds
    fun imageFile(impl: ImageFileRepositoryImpl): ImageFileRepository

    @Binds
    fun recordFile(impl: RecordFileRepositoryImpl): RecordFileRepository
}