package com.pantelisstampoulis.androidtemplateproject.network.interceptor

import com.google.common.truth.Truth.assertThat
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class LoggingInterceptorTest {

    private val mockWebServer = MockWebServer()
    private val loggedLines = mutableListOf<String>()
    private val capturingLogger = HttpLoggingInterceptor.Logger { loggedLines += it }

    // A known token, so the assertion does not depend on TMDB_API_KEY being set locally.
    private val authorizationInterceptor = Interceptor { chain ->
        chain.proceed(
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $TOKEN")
                .build(),
        )
    }

    @Before
    fun setUp() {
        mockWebServer.start()
        mockWebServer.enqueue(MockResponse().setBody("{}"))
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `debug build logs the request but redacts the Authorization header`() {
        executeRequest(isDebug = true)

        assertThat(loggedLines).isNotEmpty()
        assertThat(loggedLines.none { it.contains(TOKEN) }).isTrue()
        assertThat(loggedLines).contains("Authorization: ██")
    }

    @Test
    fun `release build logs nothing`() {
        executeRequest(isDebug = false)

        assertThat(loggedLines).isEmpty()
    }

    private fun executeRequest(isDebug: Boolean) {
        val client = OkHttpClient.Builder()
            .addInterceptor(authorizationInterceptor)
            .addInterceptor(loggingInterceptor(isDebug = isDebug, logger = capturingLogger))
            .build()

        client.newCall(Request.Builder().url(mockWebServer.url("/")).build())
            .execute()
            .close()
    }

    companion object {
        private const val TOKEN = "test-secret-token"
    }
}
