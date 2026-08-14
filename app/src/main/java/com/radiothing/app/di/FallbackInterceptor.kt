package com.radiothing.app.di

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class FallbackInterceptor(private val fallbackUrls: List<String>) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var error: IOException? = null
        
        for (url in fallbackUrls) {
            val newUrl = request.url.newBuilder()
                .scheme(if (url.startsWith("https")) "https" else "http")
                .host(url.replace("https://", "").replace("http://", "").removeSuffix("/"))
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
