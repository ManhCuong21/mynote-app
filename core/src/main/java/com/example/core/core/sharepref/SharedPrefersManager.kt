package com.example.core.core.sharepref

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPrefersManager(preferences: SharedPreferences) {
    var darkModeTheme by SharedPrefBooleanParameter(DARK_MODE_THEME, preferences, false)
    var format24Hour by SharedPrefBooleanParameter(FORMAT_TIME, preferences, false)
}

class SharedPrefParameter(
    private val key: String,
    private val preferences: SharedPreferences,
    private val default: String? = null
) : ReadWriteProperty<Any, String?> {

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
        preferences.edit { putString(key, value) }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): String? {
        return if (preferences.contains(key)) preferences.getString(key, default) else default
    }
}

class SharedPrefBooleanParameter(
    private val key: String,
    private val preferences: SharedPreferences,
    private val default: Boolean = false
) : ReadWriteProperty<Any, Boolean> {

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        preferences.edit { putBoolean(key, value) }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return if (preferences.contains(key)) preferences.getBoolean(key, default) else default
    }
}

class SharedPrefLongParameter(
    private val key: String,
    private val preferences: SharedPreferences,
    private val default: Long = -1
) : ReadWriteProperty<Any, Long> {

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        preferences.edit { putLong(key, value) }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): Long {
        return if (preferences.contains(key)) preferences.getLong(key, default) else default
    }
}

class SharedPrefIntParameter(
    private val key: String,
    private val preferences: SharedPreferences,
    private val default: Int = 0
) : ReadWriteProperty<Any, Int> {

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
        preferences.edit { putInt(key, value) }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): Int {
        return if (preferences.contains(key)) preferences.getInt(key, default) else default
    }
}
