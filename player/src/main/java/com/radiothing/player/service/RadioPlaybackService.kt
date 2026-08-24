package com.radiothing.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
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
    private var wifiLock: WifiManager.WifiLock? = null
    private var notificationProvider: DefaultMediaNotificationProvider? = null

    override fun onCreate() {
        super.onCreate()

        // — Keep CPU + Wi-Fi alive for hours on all OEMs (Xiaomi, Samsung, OnePlus doze aggressively)
        try {
            val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "RadioThing::WifiLock").apply { setReferenceCounted(false) }
        } catch (_: Exception) {}

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
            .setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_ALL)
            .build()

        exoPlayer = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
            .setHandleAudioBecomingNoisy(true) // pause on headphone unplug
            .setWakeMode(C.WAKE_MODE_NETWORK) // CPU wake lock for long playback
            .build()

        exoPlayer!!.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playerManager.onServicePlayingChanged(isPlaying, exoPlayer)
                if (isPlaying) {
                    retryCount = 0
                    try { wifiLock?.acquire() } catch (_: Exception) {}
                } else {
                    try { if (wifiLock?.isHeld == true) wifiLock?.release() } catch (_: Exception) {}
                }
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

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                playerManager.onServiceAudioSessionIdChanged(audioSessionId)
            }
        })
        // push initial session id
        playerManager.onServiceAudioSessionIdChanged(exoPlayer!!.audioSessionId)

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

        // — Full media notification: artwork, lock-screen, Android Auto, Wear OS, Bluetooth
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = NotificationChannel("radio_playback", "Radio Playback", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "RadioThing ongoing playback — survives Doze on Xiaomi/Samsung/OnePlus"
                    setShowBadge(false)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
                nm.createNotificationChannel(channel)
            }
            // Media3 will keep the service foreground with a rich notification (title/artist/artwork, play/pause/next/prev)
            val provider = DefaultMediaNotificationProvider(this)
            try { provider.setSmallIcon(android.R.drawable.ic_media_play) } catch (_: Exception) {}
            setMediaNotificationProvider(provider)
        } catch (_: Exception) {}

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
        val subtitle = buildString {
            if (station.bitrate > 0) append("${station.bitrate}k")
            if (station.codec.isNotEmpty()) { if (isNotEmpty()) append(" • "); append(station.codec.uppercase()) }
            if (station.country.isNotEmpty()) { if (isNotEmpty()) append(" • "); append(station.country) }
            if (isEmpty()) append(station.tags.take(28).ifEmpty { "LIVE" })
        }
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(station.name)
            .setArtist(subtitle)
            .setAlbumTitle(station.country.ifEmpty { "RadioThing • LIVE" })

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // START_STICKY — survives swipe-away and OEM task killers for hours; MediaSession keeps foreground via notification
        return START_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Keep alive if playing — critical for Xiaomi/MIUI, OnePlus, Samsung which nuke services on task remove
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
        // else: let MediaSession keep foreground notification alive for hours (Doze + App Standby)
    }

    override fun onDestroy() {
        try { if (wifiLock?.isHeld == true) wifiLock?.release() } catch (_: Exception) {}
        wifiLock = null
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
