package com.example.media

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class MindfulMediaService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        player?.let { exoPlayer ->
            mediaSession = MediaSession.Builder(this, exoPlayer)
                .setCallback(object : MediaSession.Callback {
                    override fun onPlaybackResumption(
                        mediaSession: MediaSession,
                        controllerInfo: MediaSession.ControllerInfo
                    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
                        val mediaItems = emptyList<MediaItem>()
                        val startPosition = MediaSession.MediaItemsWithStartPosition(
                            mediaItems,
                            0,
                            0L
                        )
                        return Futures.immediateFuture(startPosition)
                    }
                })
                .build()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        player = null
        super.onDestroy()
    }
}
