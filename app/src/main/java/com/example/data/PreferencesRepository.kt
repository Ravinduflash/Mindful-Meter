package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
        
        // Hydration Keys
        val HYDRATION_GOAL_ML = intPreferencesKey("hydration_goal_ml")
        val HYDRATION_CURRENT_ML = intPreferencesKey("hydration_current_ml")
        val HYDRATION_INTERVAL_MIN = intPreferencesKey("hydration_interval_min")
        val HYDRATION_NOTIFS_ENABLED = booleanPreferencesKey("hydration_notifs_enabled")
        val HYDRATION_LAST_LOGGED_DAY = stringPreferencesKey("hydration_last_logged_day")
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

    // Hydration Flows
    val hydrationGoalMl: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.HYDRATION_GOAL_ML] ?: 2000
        }

    val hydrationCurrentMl: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.HYDRATION_CURRENT_ML] ?: 0
        }

    val hydrationIntervalMin: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.HYDRATION_INTERVAL_MIN] ?: 0
        }

    val hydrationNotifsEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.HYDRATION_NOTIFS_ENABLED] ?: true
        }

    val hydrationLastLoggedDay: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.HYDRATION_LAST_LOGGED_DAY] ?: ""
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

    // Hydration Savers
    suspend fun saveHydrationGoal(goalMl: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HYDRATION_GOAL_ML] = goalMl
        }
    }

    suspend fun saveHydrationCurrent(currentMl: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HYDRATION_CURRENT_ML] = currentMl
        }
    }

    suspend fun saveHydrationInterval(intervalMin: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HYDRATION_INTERVAL_MIN] = intervalMin
        }
    }

    suspend fun saveHydrationNotifsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HYDRATION_NOTIFS_ENABLED] = enabled
        }
    }

    suspend fun saveHydrationLastLoggedDay(dayStr: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HYDRATION_LAST_LOGGED_DAY] = dayStr
        }
    }
}
