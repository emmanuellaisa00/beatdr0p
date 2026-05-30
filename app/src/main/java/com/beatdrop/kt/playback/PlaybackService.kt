package com.beatdrop.kt.playback

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Media3 MediaSessionService — replaces react-native-track-player.
 * Provides background playback + the system media notification automatically.
 */
class PlaybackService : MediaSessionService() {
    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        // Bind the real native EQ/BassBoost to this player's audio session.
        EqEngine.attach(player.audioSessionId)
        session = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
        EqEngine.release()
        session?.run { player.release(); release() }
        session = null
        super.onDestroy()
    }
}
