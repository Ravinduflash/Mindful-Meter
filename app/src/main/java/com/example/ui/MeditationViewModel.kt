package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MeditationViewModel : ViewModel() {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    private var playbackJob: Job? = null

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    private fun play() {
        _isPlaying.value = true
        playbackJob = viewModelScope.launch {
            while (isActive && _isPlaying.value) {
                delay(100)
                val currentProgress = _progress.value
                if (currentProgress >= 1f) {
                    _progress.value = 0f
                } else {
                    _progress.value = (currentProgress + 0.005f).coerceAtMost(1f)
                    if (_progress.value >= 1f) {
                        _isPlaying.value = false
                    }
                }
            }
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
    }

    fun setProgress(value: Float) {
        _progress.value = value.coerceIn(0f, 1f)
    }

    fun rewind() {
        _progress.value = (_progress.value - 0.1f).coerceAtLeast(0f)
    }

    fun fastForward() {
        _progress.value = (_progress.value + 0.1f).coerceAtMost(1f)
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
    }
}
