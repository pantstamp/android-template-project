package com.pantelisstampoulis.androidtemplateproject.network

import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.network.di.testNetworkModule
import com.pantelisstampoulis.androidtemplateproject.network.request.RateMovieRequest
import com.pantelisstampoulis.androidtemplateproject.network.util.loadFileText
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import java.io.IOException
import java.net.SocketTimeoutException

class RetrofitNetworkDataSourceTest : KoinTest {

    private val mockWebServer: MockWebServer by inject()
    private val dataSource: RetrofitNetworkDataSource by inject()

    @Before
    fun setUp() {
        startKoin {
            modules(testNetworkModule)
        }
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        stopKoin()
    }

    @Test
    fun `test getMovies success`() = runTest {

        // Given
        val jsonResponse = loadFileText(this, "/json/tmdb_movie_discover_success.json")
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(jsonResponse)
        mockWebServer.enqueue(mockResponse)

        // When
        val result = dataSource.getMovies()

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Success::class.java)
        val movies = (result as NetworkResult.Success).data
        assertThat(movies).isNotNull()
        assertThat(movies).isNotEmpty()
        assertThat(movies.first().id).isEqualTo(533535)
    }

    @Test
    fun `test getMovies 500 server error`() = runTest {
        // Given
        val mockResponse = MockResponse()
            .setResponseCode(500)
        mockWebServer.enqueue(mockResponse)

        // When
        val result = dataSource.getMovies()

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Error::class.java)
        val error = result as NetworkResult.Error
        assertThat(error.code).isEqualTo(500)
    }

    @Test
    fun `test getMovies network timeout`() = runTest {
        // Given
        val mockResponse = MockResponse()
            .setSocketPolicy(SocketPolicy.NO_RESPONSE) // Simulate no response from the server

        mockWebServer.enqueue(mockResponse)

        // When
        val result = dataSource.getMovies()

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Exception::class.java)
        val exception = (result as NetworkResult.Exception).exception
        assertThat(exception).isInstanceOf(SocketTimeoutException::class.java)
    }

    @Test
    fun `test getMovies malformed json`() = runTest {
       // Given
       val malformedJson = """{ "results": "this_should_be_a_list" }"""
       val mockResponse = MockResponse()
           .setResponseCode(200)
           .setBody(malformedJson)
       mockWebServer.enqueue(mockResponse)

       // When
       val result = dataSource.getMovies()

       // Then
       assertThat(result).isInstanceOf(NetworkResult.Exception::class.java)
    }

    @Test
    fun `test getMovies no network connection`() = runTest {
        // Given
        val mockResponse = MockResponse()
            .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
        mockWebServer.enqueue(mockResponse)

        // When
        val result = dataSource.getMovies()

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Exception::class.java)
        val exception = (result as NetworkResult.Exception).exception
        assertThat(exception).isInstanceOf(IOException::class.java)
    }

    @Test
    fun `test getMovies unauthorized access`() = runTest {
        // Given
        val mockResponse = MockResponse()
            .setResponseCode(401)
        mockWebServer.enqueue(mockResponse)

        // When
        val result = dataSource.getMovies()

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Error::class.java)
        val error = result as NetworkResult.Error
        assertThat(error.code).isEqualTo(401)
    }

    @Test
    fun `test rateMovie success`() = runTest {

        // Given
        val jsonResponse = loadFileText(this, "/json/api_result_success.json")
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(jsonResponse)
        mockWebServer.enqueue(mockResponse)

        // When
        val result = dataSource.rateMovie(1, RateMovieRequest(8.0F))

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Success::class.java)
    }

    @Test
    fun `test rateMovie 500 server error`() = runTest {
        // Given
        val mockResponse = MockResponse()
            .setResponseCode(500)
        mockWebServer.enqueue(mockResponse)

        // When
        val result = dataSource.rateMovie(1, RateMovieRequest(8.0F))

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Error::class.java)
        val error = result as NetworkResult.Error
        assertThat(error.code).isEqualTo(500)
    }

    @Test
    fun `test rateMovie network timeout`() = runTest {
        // Given
        val mockResponse = MockResponse()
            .setSocketPolicy(SocketPolicy.NO_RESPONSE) // Simulate no response from the server

        mockWebServer.enqueue(mockResponse)

        // When
        val result = dataSource.rateMovie(1, RateMovieRequest(8.0F))

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Exception::class.java)
        val exception = (result as NetworkResult.Exception).exception
        assertThat(exception).isInstanceOf(SocketTimeoutException::class.java)
    }

    @Test
    fun `test rateMovie malformed json`() = runTest {
        // Given
        val malformedJson = """{ "results": "this_should_be_a_list" }"""
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(malformedJson)
        mockWebServer.enqueue(mockResponse)

        // When
        val result = dataSource.rateMovie(1, RateMovieRequest(8.0F))

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Exception::class.java)
    }

    @Test
    fun `test rateMovie no network connection`() = runTest {
        // Given
        val mockResponse = MockResponse()
            .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
        mockWebServer.enqueue(mockResponse)

        // When
        val result = dataSource.rateMovie(1, RateMovieRequest(8.0F))

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Exception::class.java)
        val exception = (result as NetworkResult.Exception).exception
        assertThat(exception).isInstanceOf(IOException::class.java)
    }

    @Test
    fun `test rateMovie unauthorized access`() = runTest {
        // Given
        val mockResponse = MockResponse()
            .setResponseCode(401)
        mockWebServer.enqueue(mockResponse)

        // When
        val result = dataSource.rateMovie(1, RateMovieRequest(8.0F))

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Error::class.java)
        val error = result as NetworkResult.Error
        assertThat(error.code).isEqualTo(401)
    }

}