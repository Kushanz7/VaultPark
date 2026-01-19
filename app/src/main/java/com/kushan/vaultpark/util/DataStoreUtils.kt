package com.kushan.vaultpark.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vaultpark_prefs")

/**
 * DataStore utilities for app preferences
 */
object DataStoreUtils {

    // Preference keys
    private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    private val FIRST_TIME_SETUP = booleanPreferencesKey("first_time_setup")
    private val APP_VERSION = stringPreferencesKey("app_version")
    private val USER_ID = stringPreferencesKey("user_id")
    private val DARK_THEME_ENABLED = booleanPreferencesKey("dark_theme_enabled")

    /**
     * Check if onboarding has been completed
     */
    fun hasOnboardingCompleted(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }
    }

    /**
     * Mark onboarding as completed
     */
    suspend fun markOnboardingCompleted(context: Context) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
        }
    }

    /**
     * Reset onboarding (for dev/debug)
     */
    suspend fun resetOnboarding(context: Context) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = false
        }
    }

    /**
     * Check if first-time setup is complete
     */
    fun isFirstTimeSetupComplete(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[FIRST_TIME_SETUP] ?: false
        }
    }

    /**
     * Mark first-time setup as complete
     */
    suspend fun markFirstTimeSetupComplete(context: Context) {
        context.dataStore.edit { preferences ->
            preferences[FIRST_TIME_SETUP] = true
        }
    }

    /**
     * Get saved app version
     */
    fun getAppVersion(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[APP_VERSION]
        }
    }

    /**
     * Save app version
     */
    suspend fun saveAppVersion(context: Context, version: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_VERSION] = version
        }
    }

    /**
     * Check dark theme preference
     */
    fun isDarkThemeEnabled(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[DARK_THEME_ENABLED] ?: true
        }
    }

    /**
     * Save dark theme preference
     */
    suspend fun setDarkTheme(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME_ENABLED] = enabled
        }
    }

    /**
     * Clear all preferences (for logout)
     */
    suspend fun clearAllPreferences(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
