package com.example.mynote.domain

import com.example.mynote.domain.repository.CategoryRepository
import com.example.mynote.domain.repository.CategoryRepositoryImpl
import com.example.mynote.domain.repository.NoteRepository
import com.example.mynote.domain.repository.NoteRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface DomainModule {

    @Binds
    fun noteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    fun categoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

}