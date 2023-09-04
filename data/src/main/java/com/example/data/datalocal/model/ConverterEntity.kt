package com.example.data.datalocal.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ConverterEntity {
    @TypeConverter
    fun toStringListNote(list: List<String?>?): String? {
        if (list == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<NoteEntity?>?>() {}.type
        return gson.toJson(list, type)
    }

    @TypeConverter
    fun categoryToString(categoryEntity: CategoryEntity?): String? {
        if (categoryEntity == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<CategoryEntity?>() {}.type
        return gson.toJson(categoryEntity, type)
    }

    @TypeConverter
    fun stringToCategory(string: String?): CategoryEntity? {
        if (string == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<String?>?>() {}.type
        return gson.fromJson(string, type)
    }

    @TypeConverter
    fun toListString(value: String?): List<String?>? {
        if (value == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<String?>?>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toStringListInt(list: List<Int?>?): String? {
        if (list == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<Int?>?>() {}.type
        return gson.toJson(list, type)
    }

    @TypeConverter
    fun toListInt(value: String?): List<Int?>? {
        if (value == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<Int?>?>() {}.type
        return gson.fromJson(value, type)
    }
}