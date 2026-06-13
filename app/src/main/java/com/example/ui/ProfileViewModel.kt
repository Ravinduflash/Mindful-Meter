package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.MindfulApplication
import com.example.data.MoodRepository
import com.example.data.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ProfileViewModel(
    private val moodRepository: MoodRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    // Total check-ins (Mood registers + journal saves)
    val checkInCount: StateFlow<Int> = combine(
        moodRepository.getAllLogs(),
        moodRepository.getAllJournalEntries()
    ) { logs, journals ->
        logs.size + journals.size
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Dynamic calculated streak count
    val streakCount: StateFlow<Int> = combine(
        moodRepository.getAllLogs(),
        moodRepository.getAllJournalEntries()
    ) { logs, journals ->
        val timestamps = logs.map { it.timestamp } + journals.map { it.timestamp }
        calculateStreak(timestamps)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Completed breathing session count from DataStore
    val completedBreathingSessions: StateFlow<Int> = preferencesRepository.completedBreathingSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun incrementBreathingSessions() {
        viewModelScope.launch {
            preferencesRepository.incrementBreathingSessions()
        }
    }

    private fun calculateStreak(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0
        try {
            val localDates = timestamps.map {
                Instant.ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }.toSet()

            val today = LocalDate.now()
            val yesterday = today.minusDays(1)

            // Starting point must be today or yesterday
            var current = if (localDates.contains(today)) today else if (localDates.contains(yesterday)) yesterday else null
            if (current == null) return 0

            var streak = 1
            var checkDate = current.minusDays(1)
            while (localDates.contains(checkDate)) {
                streak++
                checkDate = checkDate.minusDays(1)
            }
            return streak
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MindfulApplication)
                ProfileViewModel(
                    moodRepository = application.container.moodRepository,
                    preferencesRepository = application.container.preferencesRepository
                )
            }
        }
    }
}
