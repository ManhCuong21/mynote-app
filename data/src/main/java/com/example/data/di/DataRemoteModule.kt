package com.example.data.di

import com.example.data.dataremote.repository.CategoryRemoteRepository
import com.example.data.dataremote.repository.CategoryRemoteRepositoryImpl
import com.example.data.dataremote.repository.NoteRemoteRepository
import com.example.data.dataremote.repository.NoteRemoteRepositoryImpl
import com.example.data.dataremote.repository.FirebaseStorageRepository
import com.example.data.dataremote.repository.FirebaseStorageRepositoryImpl
import com.example.data.dataremote.repository.UserRepository
import com.example.data.dataremote.repository.UserRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface DataRemoteModule {

    @Binds
    fun userRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    fun categoryRemoteRepository(impl: CategoryRemoteRepositoryImpl): CategoryRemoteRepository

    @Binds
    fun noteRemoteRepository(impl: NoteRemoteRepositoryImpl): NoteRemoteRepository

    @Binds
    fun storageRemoteRepository(impl: FirebaseStorageRepositoryImpl): FirebaseStorageRepository

    companion object {
        @Provides
        @Singleton
        fun firebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

        @Provides
        @Singleton
        fun firebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
    }
}