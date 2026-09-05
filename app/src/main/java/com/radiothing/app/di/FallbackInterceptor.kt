package com.radiothing.app.di

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException

class FallbackInterceptor(private val fallbackUrls: List<String>) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var error: IOException? = null

        for (url in fallbackUrls) {
            val fallbackHttpUrl = url.toHttpUrl()

            val newUrl = request.url.newBuilder()
                .scheme(fallbackHttpUrl.scheme)
                .host(fallbackHttpUrl.host)
                .port(fallbackHttpUrl.port)
                .build()

            val newRequest = request.newBuilder()
                .url(newUrl)
                .build()

            try {
                response?.close()
                response = chain.proceed(newRequest)
                if (response.isSuccessful) {
                    return response
                }
            } catch (e: IOException) {
                error = e
            }
        }

        return response ?: throw error ?: IOException("All fallback providers failed")
    }
}
