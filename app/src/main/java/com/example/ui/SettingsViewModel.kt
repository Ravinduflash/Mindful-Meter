package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.MindfulApplication
import com.example.data.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import android.content.Context
import com.example.receiver.NotificationReceiver

class SettingsViewModel(
    private val repository: PreferencesRepository,
    private val context: Context
) : ViewModel() {

    val isRemindersEnabled: StateFlow<Boolean> = repository.isRemindersEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isWellnessSyncEnabled: StateFlow<Boolean> = repository.isWellnessSyncEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun toggleReminders(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveRemindersPreference(enabled)
            if (enabled) {
                NotificationReceiver.scheduleDailyAlarm(context)
            } else {
                NotificationReceiver.cancelDailyAlarm(context)
            }
        }
    }

    fun toggleWellnessSync(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveWellnessSyncPreference(enabled)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MindfulApplication)
                SettingsViewModel(application.container.preferencesRepository, application)
            }
        }
    }
}
