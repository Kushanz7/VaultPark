package com.kushan.vaultpark.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object ThemePreferencesManager {
    private const val PREFERENCES_NAME = "theme_preferences"
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    
    // Extension property for DataStore
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = PREFERENCES_NAME
    )
    
    /**
     * Save the theme mode preference
     * @param context Context
     * @param mode Theme mode: "SYSTEM", "LIGHT", "DARK"
     */
    suspend fun saveThemeMode(context: Context, mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }
    
    /**
     * Get the stored theme mode preference
     * @param context Context
     * @return Flow emitting the stored theme mode (default: "SYSTEM")
     */
    fun getThemeMode(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[THEME_MODE_KEY] ?: "SYSTEM"
        }
    }
}
