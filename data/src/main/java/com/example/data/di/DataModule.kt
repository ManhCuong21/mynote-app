package com.example.data.di

import android.content.Context
import androidx.room.Room
import com.example.data.datalocal.dao.AppDatabase
import com.example.data.datalocal.database.CategoryDatabase
import com.example.data.datalocal.database.CategoryDatabaseImpl
import com.example.data.datalocal.database.NoteDatabase
import com.example.data.datalocal.database.NoteDatabaseImpl
import com.example.data.datalocal.repository.CategoryRepository
import com.example.data.datalocal.repository.CategoryRepositoryImpl
import com.example.data.datalocal.repository.NoteRepository
import com.example.data.datalocal.repository.NoteRepositoryImpl
import com.example.data.syncdata.manager.impl.NearbySyncManagerImpl
import com.example.data.syncdata.manager.SyncManager
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.gson.Gson
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

    @Binds
    fun bindSyncManager(impl: NearbySyncManagerImpl): SyncManager

    companion object {
        @Singleton
        @Provides
        fun provideAppDatabase(
            @ApplicationContext context: Context
        ) = Room.databaseBuilder(
                context,
                AppDatabase::class.java, "mynote.db"
            ).fallbackToDestructiveMigration(false).build()

        @Singleton
        @Provides
        fun provideGson(): Gson = Gson()

        @Singleton
        @Provides
        fun provideConnectionsClient(
            @ApplicationContext context: Context
        ): ConnectionsClient = Nearby.getConnectionsClient(context)
    }
}