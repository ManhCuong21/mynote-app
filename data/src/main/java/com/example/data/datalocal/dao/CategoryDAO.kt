package com.example.data.datalocal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.data.datalocal.model.CategoryEntity

@Dao
interface CategoryDAO {
    @Insert
    fun insertCategory(category: CategoryEntity)

    @Query("SELECT * FROM categoryEntity")
    fun getAllCategory(): List<CategoryEntity>

    @Query("SELECT * FROM categoryEntity WHERE idCategory = :categoryId")
    fun getCategoryWithId(categoryId: Int): CategoryEntity

    @Update
    fun updateCategory(category: CategoryEntity)

    @Delete
    fun deleteCategory(category: CategoryEntity)
}