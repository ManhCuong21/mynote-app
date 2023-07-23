package com.example.data.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.CategoryEntity
import com.example.data.model.ConverterEntity
import com.example.data.model.NoteEntity

@Database(entities = [CategoryEntity::class, NoteEntity::class], version = 1, exportSchema = false)
@TypeConverters(ConverterEntity::class)
abstract class AppDAO : RoomDatabase() {
    abstract fun categoryDao(): CategoryDAO
    abstract fun noteDao(): NoteDAO
}