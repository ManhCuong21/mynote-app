package com.example.mynote.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mynote.data.model.CategoryEntity

@Dao
interface CategoryDAO {
    @Insert
    fun insertCategory(category: CategoryEntity)

    @Query("SELECT * FROM categoryEntity")
    fun readAllCategory(): List<CategoryEntity>

    @Update
    fun updateCategory(category: CategoryEntity)

    @Delete
    fun deleteCategory(category: CategoryEntity)
}