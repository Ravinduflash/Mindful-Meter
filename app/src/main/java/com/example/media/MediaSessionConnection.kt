package com.example.media

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MediaSessionConnection private constructor(context: Context) {

    private val sessionToken = SessionToken(
        context,
        ComponentName(context, MindfulMediaService::class.java)
    )
    
    private val controllerFuture: ListenableFuture<MediaController> =
        MediaController.Builder(context, sessionToken).buildAsync()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentMediaId = MutableStateFlow<String?>(null)
    val currentMediaId: StateFlow<String?> = _currentMediaId.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private var mediaController: MediaController? = null
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    init {
        controllerFuture.addListener(
            Runnable {
                try {
                    val controller = controllerFuture.get()
                    mediaController = controller
                    
                    controller.addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(playing: Boolean) {
                            _isPlaying.value = playing
                            if (playing) {
                                startProgressTracker()
                            } else {
                                stopProgressTracker()
                            }
                        }

                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            _currentMediaId.value = mediaItem?.mediaId
                        }
                    })
                    
                    _isPlaying.value = controller.isPlaying
                    _currentMediaId.value = controller.currentMediaItem?.mediaId
                    if (controller.isPlaying) {
                        startProgressTracker()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { runnable -> runnable.run() }
        )
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val controller = mediaController
                if (controller != null && controller.duration > 0) {
                    _progress.value = controller.currentPosition.toFloat() / controller.duration.toFloat()
                }
                delay(250)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun play(url: String, id: String) {
        val controller = mediaController ?: return
        val mediaItem = MediaItem.Builder()
            .setMediaId(id)
            .setUri(url)
            .build()
        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun play() {
        mediaController?.play()
    }

    fun stop() {
        mediaController?.stop()
    }

    fun seekToProgress(progress: Float) {
        val controller = mediaController ?: return
        if (controller.duration > 0) {
            val seekPos = (progress * controller.duration).toLong()
            controller.seekTo(seekPos)
            _progress.value = progress
        }
    }

    fun rewind(offsetMs: Long) {
        val controller = mediaController ?: return
        val newPos = (controller.currentPosition - offsetMs).coerceAtLeast(0L)
        controller.seekTo(newPos)
        if (controller.duration > 0) {
            _progress.value = newPos.toFloat() / controller.duration.toFloat()
        }
    }

    fun fastForward(offsetMs: Long) {
        val controller = mediaController ?: return
        val duration = controller.duration
        val newPos = (controller.currentPosition + offsetMs).coerceAtMost(if (duration > 0) duration else Long.MAX_VALUE)
        controller.seekTo(newPos)
        if (duration > 0) {
            _progress.value = newPos.toFloat() / duration.toFloat()
        }
    }

    companion object {
        @Volatile
        private var instance: MediaSessionConnection? = null

        fun getInstance(context: Context): MediaSessionConnection {
            return instance ?: synchronized(this) {
                instance ?: MediaSessionConnection(context.applicationContext).also { instance = it }
            }
        }
    }
}
