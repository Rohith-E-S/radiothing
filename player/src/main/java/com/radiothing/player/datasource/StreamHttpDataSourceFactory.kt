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
            .setDefaultRequestProperties(mapOf("Icy-MetaData" to "1"))
}
