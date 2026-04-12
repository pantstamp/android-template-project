package com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import com.pantelisstampoulis.androidtemplateproject.logging.Logger
import io.mockative.Mock
import io.mockative.every
import io.mockative.mock
import io.mockative.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SaveWatchedMovieUseCaseImplTest {

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

    private val useCase = SaveWatchedMovieUseCaseImpl(
        moviesRepository = repository,
        coroutineContext = UnconfinedTestDispatcher(),
        logger = noopLogger,
    )

    private val input = SaveWatchedMovieInput(
        movieId = 1,
        title = "Test Movie",
        posterUrl = null,
        overview = null,
        publicRating = 7.5,
        releaseDate = null,
        userRating = 8,
    )

    @Test
    fun shouldEmitLoadingThenSuccessWhenRepositorySucceeds() = runTest {
        every {
            repository.saveWatchedMovie(
                input.movieId,
                input.title,
                input.posterUrl,
                input.overview,
                input.publicRating,
                input.releaseDate,
                input.userRating,
            )
        }.returns(flowOf(ResultState.Success(Unit)))

        useCase(input).test {
            assertThat(awaitItem()).isEqualTo(ResultState.Loading)
            assertThat(awaitItem()).isEqualTo(ResultState.Success(Unit))
            awaitComplete()
        }

        verify {
            repository.saveWatchedMovie(
                input.movieId,
                input.title,
                input.posterUrl,
                input.overview,
                input.publicRating,
                input.releaseDate,
                input.userRating,
            )
        }.wasInvoked()
    }

    @Test
    fun shouldEmitLoadingThenErrorWhenRepositoryThrows() = runTest {
        every {
            repository.saveWatchedMovie(
                input.movieId,
                input.title,
                input.posterUrl,
                input.overview,
                input.publicRating,
                input.releaseDate,
                input.userRating,
            )
        }.returns(flow { throw Exception("save failed") })

        useCase(input).test {
            assertThat(awaitItem()).isEqualTo(ResultState.Loading)
            val error = awaitItem()
            assertThat(error).isInstanceOf(ResultState.Error::class.java)
            awaitComplete()
        }
    }
}
