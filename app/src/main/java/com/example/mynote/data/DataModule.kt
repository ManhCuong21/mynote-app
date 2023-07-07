package com.example.mynote.data

import com.example.mynote.data.database.CategoryDatabase
import com.example.mynote.data.database.CategoryDatabaseImpl
import com.example.mynote.data.database.NoteDatabase
import com.example.mynote.data.database.NoteDatabaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    fun provideCategoryDatabase(impl: CategoryDatabaseImpl): CategoryDatabase

    @Binds
    fun provideNoteDatabase(impl: NoteDatabaseImpl): NoteDatabase
}