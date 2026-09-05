package com.radiothing.player.datasource

import androidx.media3.datasource.DefaultHttpDataSource

object StreamHttpDataSourceFactory {
    const val USER_AGENT = "RadioThing/1.0 (Android; +https://github.com/radiothing)"

    fun create(): DefaultHttpDataSource.Factory =
        DefaultHttpDataSource.Factory()
            .setUserAgent(USER_AGENT)
            .setConnectTimeoutMs(8_000)
            .setReadTimeoutMs(15_000)
            .setAllowCrossProtocolRedirects(true)
        // Deliberately NO "Icy-MetaData: 1" header: ExoPlayer's DefaultHttpDataSource
        // neither parses nor strips ICY metadata blocks, so requesting them interleaves
        // metadata bytes into the audio stream — periodic clicks/crackle on many stations.
        // Song-title display would require a dedicated ICY-parsing DataSource wrapper.
}
