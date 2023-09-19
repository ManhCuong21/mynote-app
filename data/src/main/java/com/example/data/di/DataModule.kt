package com.example.data.di

import android.content.Context
import androidx.room.Room
import com.example.data.datalocal.dao.AppDAO
import com.example.data.datalocal.database.CategoryDatabase
import com.example.data.datalocal.database.CategoryDatabaseImpl
import com.example.data.datalocal.database.NoteDatabase
import com.example.data.datalocal.database.NoteDatabaseImpl
import com.example.data.datalocal.repository.CategoryLocalRepository
import com.example.data.datalocal.repository.CategoryLocalRepositoryImpl
import com.example.data.datalocal.repository.NoteLocalRepository
import com.example.data.datalocal.repository.NoteLocalRepositoryImpl
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
    fun noteRepository(impl: NoteLocalRepositoryImpl): NoteLocalRepository

    @Binds
    fun categoryLocalRepository(impl: CategoryLocalRepositoryImpl): CategoryLocalRepository

    companion object {
        @Singleton
        @Provides
        fun provideAppDatabase(
            @ApplicationContext context: Context
        ) = Room.databaseBuilder(
            context,
            AppDAO::class.java, "database-name"
        ).fallbackToDestructiveMigration().build()
    }
}