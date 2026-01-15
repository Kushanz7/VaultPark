package com.kushan.vaultpark.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kushan.vaultpark.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthPreferencesRepository(private val context: Context) {
    
    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val VEHICLE_NUMBER_KEY = stringPreferencesKey("vehicle_number")
        private val MEMBERSHIP_TYPE_KEY = stringPreferencesKey("membership_type")
    }
    
    val userId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID_KEY]
        }
    
    val userRole: Flow<UserRole?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ROLE_KEY]?.let { UserRole.valueOf(it) }
        }
    
    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_EMAIL_KEY]
        }
    
    val userName: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_NAME_KEY]
        }
    
    val vehicleNumber: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[VEHICLE_NUMBER_KEY]
        }
    
    val membershipType: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[MEMBERSHIP_TYPE_KEY]
        }
    
    suspend fun saveUserSession(
        userId: String,
        userRole: UserRole,
        userEmail: String,
        userName: String,
        vehicleNumber: String? = null,
        membershipType: String? = null
    ) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_ROLE_KEY] = userRole.name
            preferences[USER_EMAIL_KEY] = userEmail
            preferences[USER_NAME_KEY] = userName
            vehicleNumber?.let { preferences[VEHICLE_NUMBER_KEY] = it }
            membershipType?.let { preferences[MEMBERSHIP_TYPE_KEY] = it }
        }
    }
    
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_ROLE_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(VEHICLE_NUMBER_KEY)
            preferences.remove(MEMBERSHIP_TYPE_KEY)
        }
    }
}
