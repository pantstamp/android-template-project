package com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import com.pantelisstampoulis.androidtemplateproject.logging.Logger
import com.pantelisstampoulis.androidtemplateproject.model.error.ErrorModel
import com.pantelisstampoulis.androidtemplateproject.test.doubles.model.DomainTestDoubleFactory
import io.mockative.Mock
import io.mockative.every
import io.mockative.mock
import io.mockative.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetWatchedMovieUseCaseImplTest {

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

    private val useCase = GetWatchedMovieUseCaseImpl(
        moviesRepository = repository,
        coroutineContext = UnconfinedTestDispatcher(),
        logger = noopLogger,
    )

    @Test
    fun shouldEmitLoadingThenSuccessWhenWatchedMovieFound() = runTest {
        val movieId = 42
        val watchedMovie = DomainTestDoubleFactory.provideWatchedMovieModel()
        every { repository.getWatchedMovie(movieId) }.returns(flowOf(ResultState.Success(watchedMovie)))

        useCase(movieId).test {
            assertThat(awaitItem()).isEqualTo(ResultState.Loading)
            val result = awaitItem()
            assertThat(result).isInstanceOf(ResultState.Success::class.java)
            assertThat((result as ResultState.Success).data).isEqualTo(watchedMovie)
            awaitComplete()
        }

        verify { repository.getWatchedMovie(movieId) }.wasInvoked()
    }

    @Test
    fun shouldEmitLoadingThenErrorWhenWatchedMovieNotFound() = runTest {
        val movieId = 42
        every { repository.getWatchedMovie(movieId) }
            .returns(flowOf(ResultState.Error(ErrorModel.NotFound())))

        useCase(movieId).test {
            assertThat(awaitItem()).isEqualTo(ResultState.Loading)
            val result = awaitItem()
            assertThat(result).isInstanceOf(ResultState.Error::class.java)
            assertThat((result as ResultState.Error).error).isInstanceOf(ErrorModel.NotFound::class.java)
            awaitComplete()
        }
    }
}
