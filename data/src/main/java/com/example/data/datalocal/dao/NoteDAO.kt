package com.example.data.datalocal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.data.datalocal.model.NoteEntity

@Dao
interface NoteDAO {
    @Insert
    fun insertNote(note: NoteEntity)

    @Query("SELECT * FROM noteEntity ORDER BY idNote DESC")
    fun readAllNote(): List<NoteEntity>

    @Query("SELECT * FROM NoteEntity WHERE idCategory = :idCategory ORDER BY idNote DESC")
    fun readNoteWithCategory(idCategory: Long): List<NoteEntity>

    @Update
    fun updateNote(note: NoteEntity)

    @Delete
    fun deleteNote(note: NoteEntity)
}