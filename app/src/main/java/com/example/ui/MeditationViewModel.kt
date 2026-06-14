package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.MindfulApplication
import com.example.media.MediaSessionConnection
import kotlinx.coroutines.flow.StateFlow

class MeditationViewModel : ViewModel() {

    private val mediaSessionConnection = MediaSessionConnection.getInstance(MindfulApplication.instance)

    val isPlaying: StateFlow<Boolean> = mediaSessionConnection.isPlaying
    val progress: StateFlow<Float> = mediaSessionConnection.progress

    fun togglePlayPause() {
        if (isPlaying.value) {
            mediaSessionConnection.pause()
        } else {
            // Play Zenith Meditation stream
            mediaSessionConnection.play(
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                "meditation_zenith"
            )
        }
    }

    fun setProgress(value: Float) {
        mediaSessionConnection.seekToProgress(value)
    }

    fun rewind() {
        // Rewind by 10% (60,000ms for a 10s track or typical 10m track)
        mediaSessionConnection.rewind(60000L)
    }

    fun fastForward() {
        // Fast forward by 10% (60,000ms)
        mediaSessionConnection.fastForward(60000L)
    }
}
