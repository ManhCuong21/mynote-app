package com.example.data.di

import android.content.Context
import androidx.room.Room
import com.example.data.dao.AppDAO
import com.example.data.database.CategoryDatabase
import com.example.data.database.CategoryDatabaseImpl
import com.example.data.database.NoteDatabase
import com.example.data.database.NoteDatabaseImpl
import com.example.data.repository.CategoryRepository
import com.example.data.repository.CategoryRepositoryImpl
import com.example.data.repository.NoteRepository
import com.example.data.repository.NoteRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {

    @Binds
    fun provideCategoryDatabase(impl: CategoryDatabaseImpl): CategoryDatabase

    @Binds
    fun provideNoteDatabase(impl: NoteDatabaseImpl): NoteDatabase

    @Binds
    fun noteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    fun categoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    companion object {
        @Singleton
        @Provides
        fun provideAppDatabase(
            @ApplicationContext context: Context
        ) = Room.databaseBuilder(
            context,
            AppDAO::class.java, "database-name"
        ).build()
    }
}