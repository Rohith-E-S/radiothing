package com.radiothing.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.repository.RecentlyPlayedRepository
import com.radiothing.domain.repository.SettingsRepository
import com.radiothing.player.NextPreWarmer
import com.radiothing.player.OffloadState
import com.radiothing.player.PlayerManager
import com.radiothing.player.RetryCoordinator
import com.radiothing.player.StallWatchdog
import com.radiothing.player.datasource.StreamHttpDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
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

    @Inject
    lateinit var offloadState: OffloadState

    @Inject
    lateinit var nextPreWarmer: NextPreWarmer

    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var currentStation: RadioStation? = null
    private var wifiLock: WifiManager.WifiLock? = null
    private var powerLock: PowerManager.WakeLock? = null
    private var notificationProvider: DefaultMediaNotificationProvider? = null

    @Suppress("unused")
    private var currentBufferSizeMs: Int = 5_000

    private lateinit var retryCoordinator: RetryCoordinator
    private val stallWatchdog = StallWatchdog()
    private var watchdogJob: Job? = null
    private var bufferingStartMs: Long? = null
    private val bufferingTimeoutMs: Long = 20_000L

    override fun onCreate() {
        super.onCreate()

        try {
            val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "RadioThing::WifiLock").apply { setReferenceCounted(false) }
        } catch (_: Exception) {}

        retryCoordinator = RetryCoordinator(
            scope = serviceScope,
            onRetry = { retryCurrentStation() },
            onGiveUp = { msg -> playerManager.onServiceError(com.radiothing.player.StreamErrorMessages.fromMessage(msg)) }
        )

        val initialBufferSize = readBufferSizeSync()
        currentBufferSizeMs = initialBufferSize
        val loadControl = LoadControlFactory.from(initialBufferSize)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_ALL)
            .build()

        val httpFactory = StreamHttpDataSourceFactory.create()
        val mediaSourceFactory = buildMediaSourceFactory(httpFactory)

        exoPlayer = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setSkipSilenceEnabled(false)
            .build()

        // Enable audio offload (DSP decode) to reduce CPU/battery and avoid Doze stalls
        try {
            val offloadPrefs = androidx.media3.common.TrackSelectionParameters.AudioOffloadPreferences.Builder()
                .setAudioOffloadMode(androidx.media3.common.TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED)
                .setIsGaplessSupportRequired(false)
                .setIsSpeedChangeSupportRequired(false)
                .build()
            exoPlayer!!.trackSelectionParameters = exoPlayer!!.trackSelectionParameters
                .buildUpon()
                .setAudioOffloadPreferences(offloadPrefs)
                .build()
        } catch (_: Exception) {}

        // OffloadState remains available for UI if needed; offloaded streams
        // automatically fall back to synthetic visualizer via Visualizer attach failure

        exoPlayer!!.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playerManager.onServicePlayingChanged(isPlaying, exoPlayer)
                if (isPlaying) {
                    retryCoordinator.resetCount()
                    stallWatchdog.reset()
                    startStallWatchdog()
                    acquireWakeLocks()
                } else {
                    stopStallWatchdog()
                    releaseWakeLocks()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                playerManager.onServiceBufferingChanged(playbackState == Player.STATE_BUFFERING)
                // A live server closing the connection cleanly drives STATE_ENDED
                // with no exception — without a retry the player sits in a silent
                // stopped state while the notification looks intact. The
                // playWhenReady check excludes user-initiated stops; the retry
                // budget bounds the loop if the station is really gone.
                if (playbackState == Player.STATE_ENDED &&
                    currentStation != null &&
                    exoPlayer?.playWhenReady == true
                ) {
                    playerManager.onServiceBufferingChanged(true)
                    retryCoordinator.requestRetry("Stream ended")
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                playerManager.onServiceBufferingChanged(true)
                retryCoordinator.requestRetry(com.radiothing.player.StreamErrorMessages.from(error))
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                playerManager.onServiceAudioSessionIdChanged(audioSessionId)
            }
        })
        playerManager.onServiceAudioSessionIdChanged(exoPlayer!!.audioSessionId)
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
            val provider = DefaultMediaNotificationProvider(this)
            try { provider.setSmallIcon(android.R.drawable.ic_media_play) } catch (_: Exception) {}
            setMediaNotificationProvider(provider)
        } catch (_: Exception) {}

        // Drain transient commands left over from a previous service lifetime.
        // Commands are StateFlows, so freshly launched collectors re-receive
        // whatever was last published: a stale pause=true would instantly pause
        // the playback the playCommand collector just started, and a stale
        // faded volume (mid sleep-timer fade when the service died) would mute
        // a freshly prepared player. A play command is NOT drained — publishing
        // one is why the service was started, and it must survive the restart.
        playerManager.consumePauseCommand()
        playerManager.consumeResumeCommand()
        playerManager.consumeVolumeCommand()

        serviceScope.launch {
            playerManager.playCommand.collect { cmd ->
                if (cmd != null) {
                    // User-initiated tune: drop pending retries and start with a fresh budget
                    retryCoordinator.cancel()
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

    private fun startStallWatchdog() {
        watchdogJob?.cancel()
        bufferingStartMs = null
        watchdogJob = serviceScope.launch {
            while (isActive) {
                delay(5_000L)
                val player = exoPlayer ?: break
                val isBuffering = player.playbackState == Player.STATE_BUFFERING
                // Buffering timeout — if we stay in buffering too long without progress,
                // treat it as a failure and retry (prevents infinite buffering / ANR)
                if (isBuffering) {
                    val start = bufferingStartMs
                    if (start == null) {
                        bufferingStartMs = System.currentTimeMillis()
                    } else if (System.currentTimeMillis() - start >= bufferingTimeoutMs) {
                        bufferingStartMs = null
                        playerManager.onServiceBufferingChanged(true)
                        retryCoordinator.requestRetry("Buffering timeout")
                        // don't return; still run stall check below in case both trigger
                    }
                } else {
                    bufferingStartMs = null
                }
                val pos = try { player.currentPosition } catch (_: Exception) { 0L }
                val stalled = stallWatchdog.tick(
                    currentPositionMs = pos,
                    isPlaying = player.isPlaying,
                    isBuffering = isBuffering
                )
                if (stalled) {
                    playerManager.onServiceBufferingChanged(true)
                    retryCoordinator.requestRetry("Stall detected")
                }
            }
        }
    }

    private fun stopStallWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = null
        bufferingStartMs = null
    }

    /**
     * Live radio is never routed through SimpleCache: cached bytes from a
     * previous connection are minutes-old audio that CacheDataSource would
     * serve before splicing to the live edge (the pre-warmer used to guarantee
     * such a hit). If on-demand content is ever added, caching can return for
     * those streams only.
     */
    private fun buildMediaSourceFactory(httpFactory: DefaultHttpDataSource.Factory): ProgressiveMediaSource.Factory {
        return ProgressiveMediaSource.Factory(httpFactory)
    }

    private fun readBufferSizeSync(): Int = try {
        kotlinx.coroutines.runBlocking {
            settingsRepository.getSettings().first().bufferSize
        }
    } catch (_: Exception) { 5_000 }

    private fun playStation(station: RadioStation, queue: List<RadioStation>, queueIndex: Int) {
        // Keep the retry count: this is also re-entered by retryCurrentStation(),
        // so resetting here would make maxRetries unreachable. User-initiated
        // tunes reset the budget via retryCoordinator.cancel() in the play collector.
        retryCoordinator.cancelPendingRetry()
        stallWatchdog.reset()
        bufferingStartMs = null
        currentStation = station
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
        exoPlayer?.setMediaItem(item)
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true

        serviceScope.launch {
            recentlyPlayedRepository.addRecentlyPlayed(station)
        }

        // Pre-warm only made sense when warmed bytes came back out of the shared
        // cache; playback no longer reads that cache for live streams, so there
        // is nothing to pre-warm. Any stale warm player is released.
        nextPreWarmer.cancel()
    }

    private fun retryCurrentStation() {
        val station = currentStation ?: return
        try { nextPreWarmer.cancel() } catch (_: Exception) {}
        playStation(station, emptyList(), 0)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    private fun acquireWakeLocks() {
        if (powerLock == null) {
            try {
                val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
                powerLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RadioThing::PlayerWakeLock").apply {
                    setReferenceCounted(false)
                }
            } catch (_: Exception) {}
        }
        try { powerLock?.acquire() } catch (_: Exception) {}
        try { wifiLock?.acquire() } catch (_: Exception) {}
    }

    private fun releaseWakeLocks() {
        try { if (powerLock?.isHeld == true) powerLock?.release() } catch (_: Exception) {}
        try { if (wifiLock?.isHeld == true) wifiLock?.release() } catch (_: Exception) {}
    }

    override fun onDestroy() {
        // Cancel the scope first so the command collectors stop immediately:
        // a zombie collector would consume (and silently drop) play commands
        // published after destroy, leaving the restarted service with nothing
        // to play. Commands arriving while the service is dead stay in the
        // command StateFlows and are picked up on the next onCreate.
        serviceScope.cancel()
        stopStallWatchdog()
        retryCoordinator.cancel()
        try { nextPreWarmer.cancel() } catch (_: Exception) {}
        releaseWakeLocks()
        powerLock = null
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
