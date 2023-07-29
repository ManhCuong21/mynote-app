package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.NoteEntity

@Dao
interface NoteDAO {
    @Insert
    fun insertNote(note: NoteEntity)

    @Query("SELECT * FROM noteEntity")
    fun readAllNote(): List<NoteEntity>

    @Query("SELECT * FROM NoteEntity WHERE idCategory = :categoryId")
    fun readNoteWithCategory(categoryId: Int): List<NoteEntity>

    @Update
    fun updateNote(note: NoteEntity)

    @Delete
    fun deleteNote(note: NoteEntity)
}