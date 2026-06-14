package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.MindfulApplication
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RoutineViewModel(
    private val moodRepository: MoodRepository,
    private val dailyIntentionRepository: DailyIntentionRepository,
    private val dailyHabitRepository: DailyHabitRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    // Step 1: Mood check-in state
    private val _selectedMood = MutableStateFlow("Calm")
    val selectedMood: StateFlow<String> = _selectedMood.asStateFlow()

    private val _moodNote = MutableStateFlow("")
    val moodNote: StateFlow<String> = _moodNote.asStateFlow()

    fun selectMood(mood: String) {
        _selectedMood.value = mood
    }

    fun setMoodNote(note: String) {
        _moodNote.value = note
    }

    // Step 2: Box Breathing state
    private val _isBreathingRunning = MutableStateFlow(false)
    val isBreathingRunning = _isBreathingRunning.asStateFlow()

    private val _breathingTotalSecondsElapsed = MutableStateFlow(0)
    val breathingTotalSecondsElapsed = _breathingTotalSecondsElapsed.asStateFlow()

    private val _currentPhaseIndex = MutableStateFlow(0)
    val currentPhaseIndex = _currentPhaseIndex.asStateFlow()

    private val _phaseSecondsElapsed = MutableStateFlow(0)
    val phaseSecondsElapsed = _phaseSecondsElapsed.asStateFlow()

    private var breathingJob: Job? = null

    val breathingSequence = listOf(
        BreathingState("Inhale", 4, "Breathe in slowly through your nose"),
        BreathingState("Hold", 4, "Retain your breath gently"),
        BreathingState("Exhale", 4, "Release the air fully through your mouth"),
        BreathingState("Hold", 4, "Pause in stillness with lungs empty")
    )

    fun startBreathing() {
        if (_isBreathingRunning.value) return
        _isBreathingRunning.value = true
        breathingJob = viewModelScope.launch {
            while (_isBreathingRunning.value && _breathingTotalSecondsElapsed.value < 60) {
                delay(1000)
                _breathingTotalSecondsElapsed.value += 1
                
                val currentPhase = breathingSequence[_currentPhaseIndex.value]
                val nextPhaseSeconds = _phaseSecondsElapsed.value + 1
                if (nextPhaseSeconds >= currentPhase.durationSeconds) {
                    _phaseSecondsElapsed.value = 0
                    _currentPhaseIndex.value = (_currentPhaseIndex.value + 1) % breathingSequence.size
                } else {
                    _phaseSecondsElapsed.value = nextPhaseSeconds
                }

                if (_breathingTotalSecondsElapsed.value >= 60) {
                    _isBreathingRunning.value = false
                    break
                }
            }
        }
    }

    fun pauseBreathing() {
        _isBreathingRunning.value = false
        breathingJob?.cancel()
    }

    fun resetBreathing() {
        pauseBreathing()
        _breathingTotalSecondsElapsed.value = 0
        _currentPhaseIndex.value = 0
        _phaseSecondsElapsed.value = 0
    }

    // Step 3: Intentions state
    private val _intention1 = MutableStateFlow("")
    val intention1 = _intention1.asStateFlow()

    private val _intention2 = MutableStateFlow("")
    val intention2 = _intention2.asStateFlow()

    private val _intention3 = MutableStateFlow("")
    val intention3 = _intention3.asStateFlow()

    fun setIntention1(text: String) { _intention1.value = text }
    fun setIntention2(text: String) { _intention2.value = text }
    fun setIntention3(text: String) { _intention3.value = text }

    // Batch insertion of the complete routine
    fun completeRoutine(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // 1. Insert mood check-in
            val moodLog = MoodLog(
                mood = _selectedMood.value,
                note = _moodNote.value.trim()
            )
            moodRepository.insertLog(moodLog)

            // 2. Insert breathing session (into both Preferences count and DailyHabit Database table)
            preferencesRepository.incrementBreathingSessions()
            dailyHabitRepository.insertHabit(
                DailyHabit(
                    name = "Morning Box Breathing 🧘",
                    isCompleted = true,
                    dateStr = todayStr
                )
            )

            // 3. Insert non-empty daily intentions
            if (_intention1.value.isNotBlank()) {
                dailyIntentionRepository.insertIntention(
                    DailyIntention(text = _intention1.value.trim(), isCompleted = false, dateStr = todayStr)
                )
            }
            if (_intention2.value.isNotBlank()) {
                dailyIntentionRepository.insertIntention(
                    DailyIntention(text = _intention2.value.trim(), isCompleted = false, dateStr = todayStr)
                )
            }
            if (_intention3.value.isNotBlank()) {
                dailyIntentionRepository.insertIntention(
                    DailyIntention(text = _intention3.value.trim(), isCompleted = false, dateStr = todayStr)
                )
            }

            onSuccess()
        }
    }

    override fun onCleared() {
        super.onCleared()
        breathingJob?.cancel()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MindfulApplication)
                RoutineViewModel(
                    moodRepository = application.container.moodRepository,
                    dailyIntentionRepository = application.container.dailyIntentionRepository,
                    dailyHabitRepository = application.container.dailyHabitRepository,
                    preferencesRepository = application.container.preferencesRepository
                )
            }
        }
    }
}

data class BreathingState(
    val phaseName: String,
    val durationSeconds: Int,
    val instructionText: String
)
