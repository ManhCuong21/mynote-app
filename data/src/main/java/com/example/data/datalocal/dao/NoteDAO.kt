package com.example.data.datalocal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.data.datalocal.model.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDAO {
    @Insert
    suspend fun insertNote(note: NoteEntity)

    @Query("SELECT * FROM noteEntity ORDER BY idNote DESC")
    suspend fun readAllNote(): List<NoteEntity>

    @Query("SELECT * FROM NoteEntity WHERE idCategory = :idCategory ORDER BY idNote DESC")
    suspend fun readNoteWithCategory(idCategory: Long): List<NoteEntity>

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query(
        """
    SELECT * FROM NoteEntity 
    WHERE titleNote LIKE '%' || :keyword || '%' 
       OR contentNote LIKE '%' || :keyword || '%' 
    ORDER BY idNote DESC
"""
    )

    fun searchNoteByKeyword(keyword: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM noteEntity WHERE idNote = :id LIMIT 1")
    suspend fun getNoteById(id: Long): NoteEntity?
}