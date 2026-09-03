package com.radiothing.player.cache

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var cache: SimpleCache? = null
    private var databaseProvider: StandaloneDatabaseProvider? = null

    @Synchronized
    fun get(): SimpleCache {
        cache?.let { return it }
        val cacheDir = File(context.cacheDir, "streams")
        cacheDir.mkdirs()
        val evictor = LeastRecentlyUsedCacheEvictor(64L * 1024 * 1024)
        val dbProvider = StandaloneDatabaseProvider(context).also { databaseProvider = it }
        return SimpleCache(cacheDir, evictor, dbProvider).also { cache = it }
    }

    @Synchronized
    fun release() {
        try { cache?.release() } catch (_: Exception) {}
        cache = null
        databaseProvider = null
    }
}
