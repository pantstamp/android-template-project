@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import com.pantelisstampoulis.androidtemplateproject.logging.Logger
import com.pantelisstampoulis.androidtemplateproject.model.error.DomainError
import com.pantelisstampoulis.androidtemplateproject.test.doubles.model.DomainTestDoubleFactory
import io.mockative.Mock
import io.mockative.every
import io.mockative.mock
import io.mockative.once
import io.mockative.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchMoviesUseCaseImplTest {

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

    private val useCase = SearchMoviesUseCaseImpl(
        moviesRepository = repository,
        coroutineContext = UnconfinedTestDispatcher(),
        logger = noopLogger,
    )

    @Test
    fun shouldEmitLoadingThenSuccessWhenSearchSucceeds() = runTest {
        val movies = listOf(DomainTestDoubleFactory.provideMovieModel(), DomainTestDoubleFactory.provideMovieModel())
        every { repository.searchMovies("matrix") }.returns(flowOf(ResultState.Success(movies)))

        useCase("matrix").test {
            assertThat(awaitItem()).isEqualTo(ResultState.Loading)
            assertThat(awaitItem()).isEqualTo(ResultState.Success(movies))
            awaitComplete()
        }
    }

    @Test
    fun shouldEmitLoadingThenErrorWhenRepositoryFails() = runTest {
        every { repository.searchMovies("matrix") }
            .returns(flowOf(ResultState.Error(DomainError.NoNetworkConnection())))

        useCase("matrix").test {
            assertThat(awaitItem()).isEqualTo(ResultState.Loading)
            assertThat(awaitItem()).isEqualTo(ResultState.Error(DomainError.NoNetworkConnection()))
            awaitComplete()
        }
    }

    @Test
    fun shouldEmitUnknownErrorWhenRepositoryThrows() = runTest {
        every { repository.searchMovies("matrix") }
            .returns(flow { throw IllegalStateException("boom") })

        useCase("matrix").test {
            assertThat(awaitItem()).isEqualTo(ResultState.Loading)
            val result = awaitItem()
            assertThat((result as ResultState.Error).error).isInstanceOf(DomainError.Unknown::class.java)
            awaitComplete()
        }
    }

    @Test
    fun shouldPassQueryToRepositoryUnchanged() = runTest {
        every { repository.searchMovies(" The Matrix ") }.returns(flowOf(ResultState.Success(emptyList())))

        useCase(" The Matrix ").test {
            cancelAndIgnoreRemainingEvents()
        }

        verify { repository.searchMovies(" The Matrix ") }.wasInvoked(exactly = once)
    }
}
