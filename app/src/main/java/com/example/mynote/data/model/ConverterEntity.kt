package com.example.mynote.data.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ConverterEntity {
    @TypeConverter
    fun toString(list: List<String?>?): String? {
        if (list == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<NoteEntity?>?>() {}.type
        return gson.toJson(list, type)
    }

    @TypeConverter
    fun toList(value: String?): List<String?>? {
        if (value == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<String?>?>() {}.type
        return gson.fromJson(value, type)
    }
}