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
    fun `test getMovies success when optional fields are null or empty`() = runTest {
        // Given
        val jsonResponse = loadFileText(this, "/json/tmdb_movie_discover_missing_optional_fields.json")
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(jsonResponse)
        mockWebServer.enqueue(mockResponse)

        // When
        val result = dataSource.getMovies()

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Success::class.java)
        val movies = (result as NetworkResult.Success).data
        assertThat(movies).hasSize(3)
        val movie = movies.single { it.id == 1580190 }
        assertThat(movie.backdropPath).isNull()
        assertThat(movie.posterPath).isNull()
        assertThat(movie.genreIds).isEmpty()
        assertThat(movie.releaseDate).isEmpty()
    }

    @Test
    fun `test getMovies success when optional fields are missing`() = runTest {
        // Given
        val jsonResponse = loadFileText(this, "/json/tmdb_movie_discover_missing_optional_fields.json")
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(jsonResponse)
        mockWebServer.enqueue(mockResponse)

        // When
        val result = dataSource.getMovies()

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Success::class.java)
        val movie = (result as NetworkResult.Success).data.single { it.id == 1999999 }
        assertThat(movie.backdropPath).isNull()
        assertThat(movie.posterPath).isNull()
        assertThat(movie.genreIds).isEmpty()
        assertThat(movie.releaseDate).isNull()
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

    @Test
    fun `test searchMovies success`() = runTest {
        // Given
        val jsonResponse = loadFileText(this, "/json/tmdb_search_movie_success.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        // When
        val result = dataSource.searchMovies("matrix")

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Success::class.java)
        val movies = (result as NetworkResult.Success).data
        assertThat(movies).hasSize(3)
        assertThat(movies.first().id).isEqualTo(603)
        assertThat(movies.single { it.id == 684428 }.posterPath).isNull()
        assertThat(movies.single { it.id == 1291608 }.releaseDate).isEmpty()
    }

    @Test
    fun `test searchMovies sends query, include_adult=false and page=1`() = runTest {
        // Given
        val jsonResponse = loadFileText(this, "/json/tmdb_search_movie_empty.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        // When
        dataSource.searchMovies("the matrix")

        // Then
        val request = mockWebServer.takeRequest()
        val url = request.requestUrl!!
        assertThat(url.encodedPath).isEqualTo("/search/movie")
        assertThat(url.queryParameter("query")).isEqualTo("the matrix")
        assertThat(url.queryParameter("include_adult")).isEqualTo("false")
        assertThat(url.queryParameter("page")).isEqualTo("1")
    }

    @Test
    fun `test searchMovies empty results`() = runTest {
        // Given
        val jsonResponse = loadFileText(this, "/json/tmdb_search_movie_empty.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        // When
        val result = dataSource.searchMovies("zzzzqqq")

        // Then
        assertThat(result).isEqualTo(NetworkResult.Success(emptyList<Any>()))
    }

    @Test
    fun `test searchMovies http error`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        // When
        val result = dataSource.searchMovies("matrix")

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Error::class.java)
        assertThat((result as NetworkResult.Error).code).isEqualTo(401)
    }

    @Test
    fun `test searchMovies no connection`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        // When
        val result = dataSource.searchMovies("matrix")

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Exception::class.java)
        assertThat((result as NetworkResult.Exception).exception).isInstanceOf(IOException::class.java)
    }

    @Test
    fun `test getMovie success`() = runTest {
        // Given
        val jsonResponse = loadFileText(this, "/json/tmdb_movie_details_success.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        // When
        val result = dataSource.getMovie(603)

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Success::class.java)
        val movie = (result as NetworkResult.Success).data
        assertThat(movie.id).isEqualTo(603)
        assertThat(movie.title).isEqualTo("The Matrix")
        assertThat(movie.genres).hasSize(2)
        assertThat(movie.genres.first().id).isEqualTo(28)
    }

    @Test
    fun `test getMovie requests movie path`() = runTest {
        // Given
        val jsonResponse = loadFileText(this, "/json/tmdb_movie_details_success.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        // When
        dataSource.getMovie(603)

        // Then
        assertThat(mockWebServer.takeRequest().path).isEqualTo("/movie/603")
    }

    @Test
    fun `test getMovie not found`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setResponseCode(404))

        // When
        val result = dataSource.getMovie(603)

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Error::class.java)
        assertThat((result as NetworkResult.Error).code).isEqualTo(404)
    }
}
