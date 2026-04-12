package com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import com.pantelisstampoulis.androidtemplateproject.logging.Logger
import com.pantelisstampoulis.androidtemplateproject.model.movies.WatchedMovie
import com.pantelisstampoulis.androidtemplateproject.test.doubles.model.DomainTestDoubleFactory
import io.mockative.Mock
import io.mockative.every
import io.mockative.mock
import io.mockative.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetWatchedMoviesUseCaseImplTest {

    @Mock
    private val repository = mock(MoviesRepository::class)

    private val noopLogger = object : Logger {
        override val tag: String = "test"
        override fun d(throwable: Throwable?, tag: String, message: () -> String) {}
        override fun i(throwable: Throwable?, tag: String, message: () -> String) {}
        override fun w(throwable: Throwable?, tag: String, message: () -> String) {}
        override fun e(throwable: Throwable?, tag: String, message: () -> String) {}
        override fun a(throwable: Throwable?, tag: String, message: () -> String) {}
        override fun v(throwable: Throwable?, tag: String, message: () -> String) {}
    }

    private val useCase = GetWatchedMoviesUseCaseImpl(
        moviesRepository = repository,
        coroutineContext = UnconfinedTestDispatcher(),
        logger = noopLogger,
    )

    @Test
    fun shouldEmitLoadingThenSuccessWithWatchedMovieList() = runTest {
        val watchedMovies = listOf(
            DomainTestDoubleFactory.provideWatchedMovieModel(),
            DomainTestDoubleFactory.provideWatchedMovieModel(),
        )
        every { repository.getWatchedMovies() }.returns(flowOf(ResultState.Success(watchedMovies)))

        useCase(Unit).test {
            assertThat(awaitItem()).isEqualTo(ResultState.Loading)
            val result = awaitItem()
            assertThat(result).isInstanceOf(ResultState.Success::class.java)
            assertThat((result as ResultState.Success<List<WatchedMovie>>).data).isEqualTo(watchedMovies)
            awaitComplete()
        }

        verify { repository.getWatchedMovies() }.wasInvoked()
    }

    @Test
    fun shouldEmitLoadingThenSuccessWithEmptyList() = runTest {
        every { repository.getWatchedMovies() }.returns(flowOf(ResultState.Success(emptyList())))

        useCase(Unit).test {
            assertThat(awaitItem()).isEqualTo(ResultState.Loading)
            val result = awaitItem()
            assertThat(result).isInstanceOf(ResultState.Success::class.java)
            assertThat((result as ResultState.Success<List<WatchedMovie>>).data).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun shouldEmitLoadingThenErrorWhenRepositoryThrows() = runTest {
        every { repository.getWatchedMovies() }.returns(flow { throw Exception("fetch failed") })

        useCase(Unit).test {
            assertThat(awaitItem()).isEqualTo(ResultState.Loading)
            val error = awaitItem()
            assertThat(error).isInstanceOf(ResultState.Error::class.java)
            awaitComplete()
        }
    }
}
