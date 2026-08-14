package com.radiothing.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServerResolver {
    suspend fun resolveAvailableServer(): String = withContext(Dispatchers.IO) {
        // By bypassing the IP-based resolution, we ensure the Host header is sent correctly
        // and avoid 404 Not Found errors from the RadioBrowser API load balancers.
        "https://all.api.radio-browser.info/"
    }
}
