package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.MindfulApplication
import com.example.media.MediaSessionConnection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Soundscape(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String
)

class SleepViewModel : ViewModel() {

    private val mediaSessionConnection = MediaSessionConnection.getInstance(MindfulApplication.instance)

    private val _soundscapes = MutableStateFlow(
        listOf(
            Soundscape("rain", "Gentle Rain", "Soothing soft downpour on cottage leaves", "rain"),
            Soundscape("waves", "Ocean Waves", "Rhythmic rolling surf at twilight beach", "waves"),
            Soundscape("white_noise", "Cosmic Noise", "Steady ambient signal to isolate thoughts", "noise"),
            Soundscape("crackle", "Campfire", "Crackling pine timber under cosmic stars", "fire")
        )
    )
    val soundscapes: StateFlow<List<Soundscape>> = _soundscapes.asStateFlow()

    val playingId: StateFlow<String?> = mediaSessionConnection.currentMediaId

    private val _timerOption = MutableStateFlow<String>("Off") // "15m", "30m", "1h", "Off"
    val timerOption: StateFlow<String> = _timerOption.asStateFlow()

    private val _secondsRemaining = MutableStateFlow<Int>(0)
    val secondsRemaining: StateFlow<Int> = _secondsRemaining.asStateFlow()

    private var timerJob: Job? = null

    private fun getSoundUrl(id: String): String {
        return when (id) {
            "rain" -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            "waves" -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
            "white_noise" -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
            "crackle" -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
            else -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        }
    }

    fun togglePlaying(id: String) {
        if (playingId.value == id) {
            mediaSessionConnection.stop()
            timerJob?.cancel()
        } else {
            mediaSessionConnection.play(getSoundUrl(id), id)
            // Re-trigger countdown timer if it has time left
            if (_secondsRemaining.value > 0) {
                startTimerCountdown()
            }
        }
    }

    fun setTimerOption(option: String) {
        _timerOption.value = option
        timerJob?.cancel()

        val seconds = when (option) {
            "15m" -> 15 * 60
            "30m" -> 30 * 60
            "1h" -> 60 * 60
            else -> 0
        }

        _secondsRemaining.value = seconds

        if (seconds > 0 && playingId.value != null) {
            startTimerCountdown()
        }
    }

    private fun startTimerCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_secondsRemaining.value > 0) {
                delay(1000)
                _secondsRemaining.value -= 1
                if (_secondsRemaining.value == 0) {
                    mediaSessionConnection.stop()
                    _timerOption.value = "Off"
                    break
                }
            }
        }
    }

    fun stopPlayback() {
        mediaSessionConnection.stop()
        _timerOption.value = "Off"
        _secondsRemaining.value = 0
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
