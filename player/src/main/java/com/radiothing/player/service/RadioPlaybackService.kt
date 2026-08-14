package com.radiothing.player.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import com.google.common.util.concurrent.ListenableFuture
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.repository.RecentlyPlayedRepository
import com.radiothing.domain.repository.SettingsRepository
import com.radiothing.player.PlayerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RadioPlaybackService : MediaSessionService() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var recentlyPlayedRepository: RecentlyPlayedRepository

    @Inject
    lateinit var playerManager: PlayerManager

    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var retryCount = 0
    private val maxRetries = 3
    private var currentStation: RadioStation? = null

    override fun onCreate() {
        super.onCreate()

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                15_000,   // min buffer: 15s
                50_000,   // max buffer: 50s
                2_500,    // buffer for playback start: 2.5s
                5_000     // buffer for playback after rebuffer: 5s
            ).build()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        exoPlayer = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
            .setHandleAudioBecomingNoisy(true) // pause on headphone unplug
            .build()

        exoPlayer!!.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playerManager.onServicePlayingChanged(isPlaying, exoPlayer)
                if (isPlaying) retryCount = 0
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                playerManager.onServiceBufferingChanged(playbackState == Player.STATE_BUFFERING)
                if (playbackState == Player.STATE_ENDED) {
                    autoRetry()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                if (retryCount < maxRetries) {
                    retryCount++
                    playerManager.onServiceBufferingChanged(true)
                    serviceScope.launch {
                        delay(2000L * retryCount)
                        retryCurrentStation()
                    }
                } else {
                    playerManager.onServiceError(error.message)
                }
            }
        })

        // Expose this player to PlayerManager so UI can observe it
        playerManager.attachServicePlayer(exoPlayer!!)

        val sessionActivityPendingIntent = packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val sessionBuilder = MediaSession.Builder(this, exoPlayer!!)
        sessionActivityPendingIntent?.let {
            sessionBuilder.setSessionActivity(it)
        }
        val session = sessionBuilder.build()
        mediaSession = session
        addSession(session)

        // Observe play commands from PlayerManager (from UI)
        serviceScope.launch {
            playerManager.playCommand.collect { cmd ->
                if (cmd != null) {
                    playStation(cmd.station, cmd.queue, cmd.queueIndex)
                    playerManager.consumePlayCommand()
                }
            }
        }

        serviceScope.launch {
            playerManager.pauseCommand.collect { if (it) { exoPlayer?.pause(); playerManager.consumePauseCommand() } }
        }

        serviceScope.launch {
            playerManager.resumeCommand.collect { if (it) { exoPlayer?.play(); playerManager.consumeResumeCommand() } }
        }

        serviceScope.launch {
            playerManager.volumeCommand.collect { vol ->
                if (vol >= 0f) { exoPlayer?.volume = vol; playerManager.consumeVolumeCommand() }
            }
        }
    }

    private fun playStation(station: RadioStation, queue: List<RadioStation>, queueIndex: Int) {
        currentStation = station
        retryCount = 0
        val url = if (station.urlResolved.isNotEmpty()) station.urlResolved else station.url
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(station.name)
            .setArtist(station.tags.take(32).ifEmpty { station.country })
            .setGenre(station.country)

        if (station.favicon.isNotBlank()) {
            runCatching { Uri.parse(station.favicon) }.getOrNull()?.let { artworkUri ->
                metadataBuilder.setArtworkUri(artworkUri)
            }
        }

        val item = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(metadataBuilder.build())
            .build()
        exoPlayer?.stop()
        exoPlayer?.setMediaItem(item)
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true

        serviceScope.launch {
            recentlyPlayedRepository.addRecentlyPlayed(station)
        }
    }

    private fun autoRetry() {
        if (currentStation != null && retryCount < maxRetries) {
            retryCount++
            serviceScope.launch {
                delay(2000L)
                retryCurrentStation()
            }
        }
    }

    private fun retryCurrentStation() {
        val station = currentStation ?: return
        playStation(station, emptyList(), 0)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.let { session ->
            removeSession(session)
            session.player.release()
            session.release()
        }
        mediaSession = null
        exoPlayer = null
        playerManager.detachServicePlayer()
        super.onDestroy()
    }
}
