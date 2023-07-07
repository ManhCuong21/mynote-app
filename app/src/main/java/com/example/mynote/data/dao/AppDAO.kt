package com.example.mynote.data.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mynote.data.model.CategoryEntity
import com.example.mynote.data.model.ConverterEntity
import com.example.mynote.data.model.NoteEntity

@Database(entities = [CategoryEntity::class, NoteEntity::class], version = 1)
@TypeConverters(ConverterEntity::class)
abstract class AppDAO : RoomDatabase() {
    abstract fun categoryDao(): CategoryDAO
    abstract fun noteDao(): NoteDAO
}