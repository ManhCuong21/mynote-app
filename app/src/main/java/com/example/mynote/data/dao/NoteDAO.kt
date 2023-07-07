package com.example.mynote.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mynote.data.model.NoteEntity
import com.github.michaelbull.result.Result
import java.lang.RuntimeException

@Dao
interface NoteDAO {
    @Insert
    fun insertNote(note: NoteEntity)

    @Query("SELECT * FROM noteEntity")
    fun readAllNote(): List<NoteEntity>

    @Update
    fun updateNote(note: NoteEntity)

    @Delete
    fun deleteNote(note: NoteEntity)
}