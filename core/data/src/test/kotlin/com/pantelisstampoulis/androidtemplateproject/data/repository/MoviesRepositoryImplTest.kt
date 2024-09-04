package com.pantelisstampoulis.androidtemplateproject.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.data.di.testDataModule
import com.pantelisstampoulis.androidtemplateproject.data.mapper.Mappers
import com.pantelisstampoulis.androidtemplateproject.database.DatabaseDataSource
import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import com.pantelisstampoulis.androidtemplateproject.model.error.ErrorModel
import com.pantelisstampoulis.androidtemplateproject.network.NetworkDataSource
import com.pantelisstampoulis.androidtemplateproject.network.NetworkResult
import com.pantelisstampoulis.androidtemplateproject.network.request.RateMovieRequest
import com.pantelisstampoulis.androidtemplateproject.network.response.ApiResultResponse
import com.pantelisstampoulis.androidtemplateproject.test.doubles.database.DatabaseTestDoubleFactory
import com.pantelisstampoulis.androidtemplateproject.test.doubles.network.NetworkTestDoubleFactory
import io.mockative.Mock
import io.mockative.classOf
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.once
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

class MoviesRepositoryImplTest : KoinTest {

    @Mock
    private val databaseDataSource = mock(classOf<DatabaseDataSource>())

    @Mock
    private val networkDataSource = mock(classOf<NetworkDataSource>())

    private val repository: MoviesRepository by inject()

    private val dataMappers: Mappers by inject()

    @Before
    fun setUp() {
        val mockModule = module {
            single {
                databaseDataSource
            } bind DatabaseDataSource::class
            single {
                networkDataSource
            } bind NetworkDataSource::class
        }
        startKoin {
            modules(testDataModule, mockModule)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun shouldFetchMoviesFromNetworkWhenDatabaseIsEmpty() = runTest {
        coEvery { databaseDataSource.getMovies() }.returns(value = flowOf(emptyList()))
        coEvery { networkDataSource.getMovies() }.returns(
            value = NetworkResult.Success(emptyList()),
        )
        repository.getMovies().test {
            awaitItem()
            coVerify { databaseDataSource.getMovies() }.wasInvoked(exactly = once)
            coVerify { networkDataSource.getMovies() }.wasInvoked(exactly = once)
            awaitComplete()
        }
    }

    @Test
    fun shouldReturnMoviesFromDatabaseWhenCacheIsNotIgnored() = runTest {
        // Prepare mock data
        val mockMovieDbList = listOf(
            DatabaseTestDoubleFactory.provideMovieDbModel(),
            DatabaseTestDoubleFactory.provideMovieDbModel()
        )
        val mockMovieList = mockMovieDbList.map { dataMappers.movieDomainMapper.fromDbToDomain(it) }

        coEvery { databaseDataSource.getMovies() }.returns(flowOf(mockMovieDbList))

        repository.getMovies(ignoreCache = false).test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(ResultState.Success::class.java)
            assertThat((result as ResultState.Success).data).isEqualTo(mockMovieList)
            coVerify { databaseDataSource.getMovies() }.wasInvoked(exactly = once)
            coVerify { networkDataSource.getMovies() }.wasNotInvoked()
            awaitComplete()
        }
    }

    @Test
    fun shouldFetchMoviesFromNetworkWhenCacheIsIgnored() = runTest {
        // Mock data in the database
        val mockMovieDbList = listOf(
            DatabaseTestDoubleFactory.provideMovieDbModel(),
            DatabaseTestDoubleFactory.provideMovieDbModel()
        )
        coEvery { databaseDataSource.getMovies() }.returns(flowOf(mockMovieDbList))

        // Mock network response
        val mockMovieListFromNetwork = listOf(
            NetworkTestDoubleFactory.provideMovieApiModel(),
            NetworkTestDoubleFactory.provideMovieApiModel()
        )
        coEvery { networkDataSource.getMovies() }.returns(NetworkResult.Success(mockMovieListFromNetwork))

        // data that should be returned by the repository
        val mockMovieDbListNew = mockMovieListFromNetwork.map {
            dataMappers.movieDataMapper.fromApiToDb(it)
        }
        val mockMovieList = mockMovieDbListNew.map { dataMappers.movieDomainMapper.fromDbToDomain(it) }

        repository.getMovies(ignoreCache = true).test {
            val result = awaitItem()
            coVerify { databaseDataSource.getMovies() }.wasInvoked(exactly = once)
            coVerify { networkDataSource.getMovies() }.wasInvoked(exactly = once)
            assertThat((result as ResultState.Success).data).isEqualTo(mockMovieList)
            awaitComplete()
        }
    }

    @Test
    fun shouldReturnErrorWhenFetchingMoviesFromNetworkFails() = runTest {
        // Mock network error
        val networkError = NetworkResult.Error(500, "Internal Server Error")

        coEvery { databaseDataSource.getMovies() }.returns(flowOf(emptyList()))
        coEvery { networkDataSource.getMovies() }.returns(networkError)

        repository.getMovies(ignoreCache = true).test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(ResultState.Error::class.java)
            coVerify { networkDataSource.getMovies() }.wasInvoked(exactly = once)
            awaitComplete()
        }
    }

    @Test
    fun shouldReturnMovieFromDatabaseWhenMovieExists() = runTest {
        val movieId = 1
        val mockMovieDbModel = DatabaseTestDoubleFactory.provideMovieDbModel()

        coEvery { databaseDataSource.getMovie(movieId) }.returns(mockMovieDbModel)
        val mockMovieDomainModel = dataMappers.movieDomainMapper.fromDbToDomain(mockMovieDbModel)

        repository.getMovie(movieId).test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(ResultState.Success::class.java)
            assertThat((result as ResultState.Success).data).isEqualTo(mockMovieDomainModel)
            coVerify { databaseDataSource.getMovie(movieId) }.wasInvoked(exactly = once)
            awaitComplete()
        }
    }

    @Test
    fun shouldReturnErrorWhenMovieNotFoundInDatabase() = runTest {
        val movieId = 1
        coEvery { databaseDataSource.getMovie(movieId) }.returns(null)

        repository.getMovie(movieId).test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(ResultState.Error::class.java)
            assertThat((result as ResultState.Error).error).isInstanceOf(ErrorModel.NotFound::class.java)
            coVerify { databaseDataSource.getMovie(movieId) }.wasInvoked(exactly = once)
            awaitComplete()
        }
    }

    @Test
    fun shouldRateMovieSuccessfullyWhenNetworkCallSucceeds() = runTest {
        val movieId = 1
        val rating = 4.5f

        coEvery { networkDataSource.rateMovie(movieId, RateMovieRequest(rating)) }
            .returns(NetworkResult.Success(ApiResultResponse(success = true, statusCode = 200, message = "")))

        repository.rateMovie(movieId, rating).test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(ResultState.Success::class.java)
            coVerify { networkDataSource.rateMovie(movieId, RateMovieRequest(rating)) }.wasInvoked(exactly = once)
            awaitComplete()
        }
    }

    @Test
    fun shouldReturnErrorWhenRatingMovieFails() = runTest {
        val movieId = 1
        val rating = 4.5f
        val networkError = NetworkResult.Error(500, "Internal Server Error")

        coEvery { networkDataSource.rateMovie(movieId, RateMovieRequest(rating)) }
            .returns(networkError)

        repository.rateMovie(movieId, rating).test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(ResultState.Error::class.java)
            assertThat((result as ResultState.Error).error).isInstanceOf(ErrorModel.ServerError::class.java)
            coVerify { networkDataSource.rateMovie(movieId, RateMovieRequest(rating)) }.wasInvoked(exactly = once)
            awaitComplete()
        }
    }
}