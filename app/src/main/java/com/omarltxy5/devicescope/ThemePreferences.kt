package com.omarltxy5.devicescope

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensions for DataStore should be at the top level
private val Context.dataStore by preferencesDataStore("settings")

object ThemePreferences {
    private val THEME_KEY = stringPreferencesKey("theme")

    fun getTheme(context: Context): Flow<ThemeMode> {
        return context.dataStore.data.map { preferences ->
            // Use the enum's valueOf or a safe when block
            when (preferences[THEME_KEY]) {
                "LIGHT" -> ThemeMode.LIGHT
                "DARK" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }
    }

    suspend fun setTheme(context: Context, mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode.name
        }
    }
}