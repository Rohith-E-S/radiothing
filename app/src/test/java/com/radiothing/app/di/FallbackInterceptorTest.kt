package com.radiothing.app.di

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.UnknownHostException

class FallbackInterceptorTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun client(urls: List<String>): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(FallbackInterceptor(urls))
            .build()

    @Test
    fun `returns successful response from first url`() {
        server.enqueue(MockResponse().setBody("ok"))

        val url = server.url("/").toString()
        val response = client(listOf(url)).newCall(
            Request.Builder().url(url).build()
        ).execute()

        assertEquals("ok", response.body?.string())
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `falls back to next url when first fails with io error`() {
        val deadServer = MockWebServer()
        deadServer.start()
        val deadUrl = deadServer.url("/").toString().removeSuffix("/")
        deadServer.shutdown() // kills the port — connection refused

        server.enqueue(MockResponse().setBody("from fallback"))

        val response = client(listOf(deadUrl, server.url("/").toString()))
            .newCall(Request.Builder().url(deadUrl).build())
            .execute()

        assertEquals("from fallback", response.body?.string())
    }

    @Test
    fun `falls back when first url returns http error`() {
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setBody("recovered"))

        val url = server.url("/").toString()
        val response = client(listOf(url, url)).newCall(
            Request.Builder().url(url).build()
        ).execute()

        assertEquals("recovered", response.body?.string())
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `rewrites host and port of request to fallback url`() {
        server.enqueue(MockResponse().setBody("rewritten"))

        // Request targets example host; interceptor must rewrite it to the MockWebServer
        val request = Request.Builder()
            .url("https://de1.api.radio-browser.info/json/stations/topclick/5")
            .build()
        val fallbackHost = server.url("/").toString()

        val response = client(listOf(fallbackHost)).newCall(request).execute()

        assertEquals("rewritten", response.body?.string())
        val recorded = server.takeRequest()
        assertEquals(server.hostName, recorded.requestUrl?.host)
    }

    @Test
    fun `throws when all urls fail`() {
        val deadServer = MockWebServer()
        deadServer.start()
        val deadUrl = deadServer.url("/").toString().removeSuffix("/")
        deadServer.shutdown()

        var thrown: Exception? = null
        try {
            client(listOf(deadUrl, deadUrl)).newCall(
                Request.Builder().url(deadUrl).build()
            ).execute()
        } catch (e: Exception) {
            thrown = e
        }
        assertTrue(thrown is UnknownHostException || thrown is java.io.IOException)
    }
}
