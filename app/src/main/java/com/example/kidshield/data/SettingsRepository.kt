package com.example.kidshield.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create a DataStore instance at the top level
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val ALLOWED_APPS_KEY = stringSetPreferencesKey("allowed_apps")
        val PIN_KEY = stringPreferencesKey("auth_pin")
        val HAS_SETUP_KEY = booleanPreferencesKey("has_setup")
    }

    val allowedAppsFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[ALLOWED_APPS_KEY] ?: emptySet()
        }

    val pinFlow: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PIN_KEY] }

    val hasSetupFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[HAS_SETUP_KEY] ?: false }

    // Add an app to the allowed list
    suspend fun addAllowedApp(packageName: String) {
        context.dataStore.edit { preferences ->
            val currentSet = preferences[ALLOWED_APPS_KEY] ?: emptySet()
            preferences[ALLOWED_APPS_KEY] = currentSet + packageName
        }
    }

    // Remove an app from the allowed list
    suspend fun removeAllowedApp(packageName: String) {
        context.dataStore.edit { preferences ->
            val currentSet = preferences[ALLOWED_APPS_KEY] ?: emptySet()
            preferences[ALLOWED_APPS_KEY] = currentSet - packageName
        }
    }

    suspend fun savePin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[PIN_KEY] = pin
            preferences[HAS_SETUP_KEY] = true
        }
    }
}
