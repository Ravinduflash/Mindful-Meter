package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.MindfulApplication
import com.example.data.FocusSession
import com.example.data.FocusSessionRepository
import com.example.media.MediaSessionConnection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FocusSoundscape(val id: String, val name: String, val url: String, val icon: String)

class FocusViewModel(private val repository: FocusSessionRepository) : ViewModel() {

    private val mediaSessionConnection = MediaSessionConnection.getInstance(MindfulApplication.instance)

    val isMediaPlaying: StateFlow<Boolean> = mediaSessionConnection.isPlaying

    val soundscapes = listOf(
        FocusSoundscape("cosmic_noise", "Cosmic Noise", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3", "🪐"),
        FocusSoundscape("spring_rain", "Spring Rain", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", "💧"),
        FocusSoundscape("ocean_waves", "Ocean Waves", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3", "🌊"),
        FocusSoundscape("forest_canopy", "Forest Canopy", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3", "🌳"),
        FocusSoundscape("campfire_spark", "Campfire Spark", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3", "🔥")
    )

    private val _selectedSoundscape = MutableStateFlow(soundscapes[0])
    val selectedSoundscape: StateFlow<FocusSoundscape> = _selectedSoundscape.asStateFlow()

    // Timer States
    private val _timerSecondsRemaining = MutableStateFlow(25 * 60) // 25 mins Focus initial
    val timerSecondsRemaining: StateFlow<Int> = _timerSecondsRemaining.asStateFlow()

    private val _timerDurationTotalSeconds = MutableStateFlow(25 * 60)
    val timerDurationTotalSeconds: StateFlow<Int> = _timerDurationTotalSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _sessionType = MutableStateFlow("Focus") // "Focus" or "Break"
    val sessionType: StateFlow<String> = _sessionType.asStateFlow()

    // Analytics: Flow of completed sessions
    val focusSessions: StateFlow<List<FocusSession>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalFocusMinutes: StateFlow<Int> = repository.totalFocusMinutesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private var countdownJob: Job? = null

    fun selectSoundscape(soundscape: FocusSoundscape) {
        _selectedSoundscape.value = soundscape
        if (_isTimerRunning.value) {
            // Hot swap play stream
            mediaSessionConnection.play(soundscape.url, soundscape.id)
        }
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true

        // Play soundscape if playing focus block
        mediaSessionConnection.play(_selectedSoundscape.value.url, _selectedSoundscape.value.id)

        countdownJob = viewModelScope.launch {
            while (_timerSecondsRemaining.value > 0) {
                delay(1000L)
                _timerSecondsRemaining.value = _timerSecondsRemaining.value - 1
            }
            onTimerComplete()
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        countdownJob?.cancel()
        mediaSessionConnection.pause()
    }

    fun stopOrSurrenderTimer() {
        _isTimerRunning.value = false
        countdownJob?.cancel()
        mediaSessionConnection.stop()

        // Reset based on current type
        resetTimerToDefault()
    }

    fun setTimerConfiguration(minutes: Int, type: String) {
        pauseTimer()
        _sessionType.value = type
        val targetSeconds = minutes * 60
        _timerDurationTotalSeconds.value = targetSeconds
        _timerSecondsRemaining.value = targetSeconds
    }

    private fun resetTimerToDefault() {
        val minutes = if (_sessionType.value == "Focus") 25 else 5
        val targetSeconds = minutes * 60
        _timerDurationTotalSeconds.value = targetSeconds
        _timerSecondsRemaining.value = targetSeconds
    }

    private fun onTimerComplete() {
        _isTimerRunning.value = false
        countdownJob?.cancel()
        mediaSessionConnection.stop()

        // Log to database completed focus blocks
        viewModelScope.launch {
            val minutesLogged = _timerDurationTotalSeconds.value / 60
            val sessionEntity = FocusSession(
                durationMinutes = minutesLogged,
                sessionType = _sessionType.value,
                soundScapeName = _selectedSoundscape.value.name,
                dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            repository.insert(sessionEntity)

            // Auto toggle session types
            if (_sessionType.value == "Focus") {
                // Completed focus, switch to break
                _sessionType.value = "Break"
                _timerDurationTotalSeconds.value = 5 * 60
                _timerSecondsRemaining.value = 5 * 60
            } else {
                // Completed break, switch back to focus
                _sessionType.value = "Focus"
                _timerDurationTotalSeconds.value = 25 * 60
                _timerSecondsRemaining.value = 25 * 60
            }
        }
    }

    fun toggleMediaPlayback() {
        if (isMediaPlaying.value) {
            mediaSessionConnection.pause()
        } else {
            mediaSessionConnection.play()
        }
    }

    fun rewindMedia() {
        mediaSessionConnection.rewind(15000L) // Rewind 15 seconds
    }

    fun forwardMedia() {
        mediaSessionConnection.fastForward(15000L) // Forward 15 seconds
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MindfulApplication)
                FocusViewModel(application.container.focusSessionRepository)
            }
        }
    }
}
