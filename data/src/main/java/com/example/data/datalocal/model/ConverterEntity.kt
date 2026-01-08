package com.example.data.datalocal.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CategoryConverter {
    private val gson = Gson()

    @TypeConverter
    fun categoryToString(categoryEntity: CategoryEntity?): String? {
        // ... (Code cũ)
        val type = object : TypeToken<CategoryEntity?>() {}.type
        return gson.toJson(categoryEntity, type)
    }

    @TypeConverter
    fun stringToCategory(string: String?): CategoryEntity? {
        // ... (Code cũ)
        val type = object : TypeToken<CategoryEntity?>() {}.type
        return gson.fromJson(string, type)
    }
}

class ListStringConverter {
    private val gson = Gson()

    @TypeConverter
    fun toStringListString(list: List<String?>?): String? {
        // ... (Code cũ)
        val type = object : TypeToken<List<String?>?>() {}.type
        return gson.toJson(list, type)
    }

    @TypeConverter
    fun toListString(value: String?): List<String?>? {
        // ... (Code cũ)
        val type = object : TypeToken<List<String?>?>() {}.type
        return gson.fromJson(value, type)
    }
}

class ListIntConverter {
    private val gson = Gson()

    @TypeConverter
    fun toStringListInt(list: List<Int>?): String? {
        if (list == null) return null
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.toJson(list, type)
    }

    @TypeConverter
    fun toListInt(value: String?): List<Int>? {
        if (value == null) return null
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, type)
    }
}