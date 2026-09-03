package com.radiothing.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.radiothing.domain.model.RadioStation
import com.radiothing.player.cache.StreamCache
import com.radiothing.player.datasource.StreamHttpDataSourceFactory
import com.radiothing.player.service.LoadControlFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pre-warms the next station in the queue by buffering it in a secondary
 * ExoPlayer. Only active when enablePreWarm is true in settings.
 * Uses the same HTTP factory and cache as the main player so the warmed
 * data is immediately available when the user taps next.
 */
@Singleton
class NextPreWarmer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val streamCache: StreamCache
) {
    private var warmPlayer: ExoPlayer? = null
    private var warmedStationUuid: String? = null

    fun warm(station: RadioStation, enableCache: Boolean) {
        cancel()
        warmedStationUuid = station.stationUuid
        try {
            val httpFactory = StreamHttpDataSourceFactory.create()
            val dataSourceFactory = if (enableCache) {
                try {
                    val cache = streamCache.get()
                    CacheDataSource.Factory()
                        .setCache(cache)
                        .setUpstreamDataSourceFactory(httpFactory)
                        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                } catch (_: Exception) { httpFactory }
            } else httpFactory

            val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
            val url = if (station.urlResolved.isNotEmpty()) station.urlResolved else station.url
            val item = MediaItem.Builder().setUri(url).build()

            val player = ExoPlayer.Builder(context)
                .setLoadControl(LoadControlFactory.from(5_000))
                .setMediaSourceFactory(mediaSourceFactory)
                .build()
            player.setMediaItem(item)
            player.prepare()
            // Don't autoplay — just buffer via shared cache
            player.playWhenReady = false
            player.volume = 0f
            warmPlayer = player
        } catch (_: Exception) {
            cancel()
        }
    }

    fun cancel() {
        try { warmPlayer?.release() } catch (_: Exception) {}
        warmPlayer = null
        warmedStationUuid = null
    }

    fun isWarmedFor(stationUuid: String): Boolean = warmedStationUuid == stationUuid
}
