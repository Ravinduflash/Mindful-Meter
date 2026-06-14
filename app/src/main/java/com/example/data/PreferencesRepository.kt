package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mindful_meter_preferences")

class PreferencesRepository(private val dataStore: DataStore<Preferences>) {
    private object PreferencesKeys {
        val MINDFUL_REMINDERS = booleanPreferencesKey("mindful_reminders")
        val COMPLETED_BREATHING_SESSIONS = intPreferencesKey("completed_breathing_sessions")
        val SYNC_WELLNESS_DATA = booleanPreferencesKey("sync_wellness_data")
    }

    val isRemindersEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.MINDFUL_REMINDERS] ?: false
        }

    val isWellnessSyncEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SYNC_WELLNESS_DATA] ?: false
        }

    val completedBreathingSessions: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.COMPLETED_BREATHING_SESSIONS] ?: 0
        }

    suspend fun saveRemindersPreference(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MINDFUL_REMINDERS] = enabled
        }
    }

    suspend fun saveWellnessSyncPreference(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SYNC_WELLNESS_DATA] = enabled
        }
    }

    suspend fun incrementBreathingSessions() {
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.COMPLETED_BREATHING_SESSIONS] ?: 0
            preferences[PreferencesKeys.COMPLETED_BREATHING_SESSIONS] = current + 1
        }
    }
}
