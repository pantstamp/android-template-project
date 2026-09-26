@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetMoviesUseCase
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper.MovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.model.error.DomainError
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import com.pantelisstampoulis.androidtemplateproject.test.doubles.model.DomainTestDoubleFactory
import io.mockative.Mock
import io.mockative.any
import io.mockative.every
import io.mockative.mock
import io.mockative.once
import io.mockative.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

class MovieListViewModelTest : KoinTest {

    @Mock
    private val getMoviesUseCase = mock(GetMoviesUseCase::class)

    private val uiMapper: MovieUiMapper by inject()

    // Lazy: construction starts the initial load, so stub the use case before touching it.
    private val viewModel: MovieListViewModel by inject()

    // Use TestDispatcher for coroutines testing
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Configure Koin for dependency injection
        val mockModule = module {
            single { getMoviesUseCase }
            single { MovieUiMapper() }
            single { MovieListViewModel(get(), get()) }
        }

        startKoin {
            modules(mockModule)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    @Test
    fun shouldShowLoadingThenMoviesOnInitialLoad() = runTest {
        val movies = provideMovies()
        every { getMoviesUseCase(false) }.returns(delayedSuccess(movies))

        val vm = viewModel
        runCurrent()

        assertThat(vm.viewState.value.isLoading).isTrue()
        assertThat(vm.viewState.value.data).isNull()

        advanceUntilIdle()

        val state = vm.viewState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
        assertThat(state.data).isEqualTo(movies.toUi())
        verify { getMoviesUseCase(false) }.wasInvoked(exactly = once)
    }

    @Test
    fun shouldShowOfflineErrorWhenFirstLoadFailsWithNoNetwork() = runTest {
        every { getMoviesUseCase(false) }
            .returns(flowOf(ResultState.Error(DomainError.NoNetworkConnection())))

        val vm = viewModel
        advanceUntilIdle()

        val state = vm.viewState.value
        assertThat(state.error).isEqualTo(MovieListError.Offline)
        assertThat(state.isLoading).isFalse()
        assertThat(state.data).isNull()
        vm.effect.test {
            expectNoEvents()
        }
    }

    @Test
    fun shouldShowGenericErrorWhenFirstLoadFailsWithOtherError() = runTest {
        every { getMoviesUseCase(false) }
            .returns(flowOf(ResultState.Error(DomainError.ServerError())))

        val vm = viewModel
        advanceUntilIdle()

        assertThat(vm.viewState.value.error).isEqualTo(MovieListError.Generic)
    }

    @Test
    fun shouldShowEmptyStateWhenFirstLoadReturnsNoMovies() = runTest {
        every { getMoviesUseCase(false) }.returns(flowOf(ResultState.Success(emptyList())))

        val vm = viewModel
        advanceUntilIdle()

        val state = vm.viewState.value
        assertThat(state.data).isEmpty()
        assertThat(state.error).isNull()
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun shouldShowLoadingThenMoviesWhenRetryingAfterFailure() = runTest {
        val movies = provideMovies()
        every { getMoviesUseCase(false) }
            .returns(flowOf(ResultState.Error(DomainError.ServerError())))
        every { getMoviesUseCase(true) }.returns(delayedSuccess(movies))

        val vm = viewModel
        advanceUntilIdle()

        vm.setEvent(MovieListEvent.Refresh)
        runCurrent()

        assertThat(vm.viewState.value.isLoading).isTrue()
        assertThat(vm.viewState.value.isRefreshing).isFalse()
        assertThat(vm.viewState.value.error).isNull()

        advanceUntilIdle()

        assertThat(vm.viewState.value.data).isEqualTo(movies.toUi())
        assertThat(vm.viewState.value.error).isNull()
        assertThat(vm.viewState.value.isLoading).isFalse()
    }

    @Test
    fun shouldShowErrorAgainWhenRetryFails() = runTest {
        every { getMoviesUseCase(false) }
            .returns(flowOf(ResultState.Error(DomainError.ServerError())))
        every { getMoviesUseCase(true) }
            .returns(flowOf(ResultState.Error(DomainError.NoNetworkConnection())))

        val vm = viewModel
        advanceUntilIdle()

        vm.setEvent(MovieListEvent.Refresh)
        advanceUntilIdle()

        assertThat(vm.viewState.value.error).isEqualTo(MovieListError.Offline)
        assertThat(vm.viewState.value.isLoading).isFalse()
    }

    @Test
    fun shouldRefreshWithoutFullScreenLoadingWhenMoviesShowing() = runTest {
        val oldMovies = provideMovies()
        val newMovies = provideMovies()
        every { getMoviesUseCase(false) }.returns(flowOf(ResultState.Success(oldMovies)))
        every { getMoviesUseCase(true) }.returns(delayedSuccess(newMovies))

        val vm = viewModel
        advanceUntilIdle()

        vm.setEvent(MovieListEvent.Refresh)
        runCurrent()

        assertThat(vm.viewState.value.isRefreshing).isTrue()
        assertThat(vm.viewState.value.isLoading).isFalse()
        assertThat(vm.viewState.value.data).isEqualTo(oldMovies.toUi())

        advanceUntilIdle()

        assertThat(vm.viewState.value.isRefreshing).isFalse()
        assertThat(vm.viewState.value.data).isEqualTo(newMovies.toUi())
    }

    @Test
    fun shouldKeepMoviesAndSendRefreshFailedWhenRefreshFails() = runTest {
        val movies = provideMovies()
        every { getMoviesUseCase(false) }.returns(flowOf(ResultState.Success(movies)))
        every { getMoviesUseCase(true) }
            .returns(flowOf(ResultState.Error(DomainError.NoNetworkConnection())))

        val vm = viewModel
        advanceUntilIdle()

        vm.setEvent(MovieListEvent.Refresh)
        advanceUntilIdle()

        val state = vm.viewState.value
        assertThat(state.data).isEqualTo(movies.toUi())
        assertThat(state.error).isNull()
        assertThat(state.isRefreshing).isFalse()
        assertThat(state.isLoading).isFalse()
        vm.effect.test {
            assertThat(awaitItem()).isEqualTo(MovieListSideEffect.RefreshFailed(MovieListError.Offline))
        }
    }

    @Test
    fun shouldApplyOnlyLatestLoadWhenRefreshStartsDuringLoad() = runTest {
        val staleMovies = provideMovies()
        val latestMovies = provideMovies()
        every { getMoviesUseCase(false) }.returns(delayedSuccess(staleMovies))
        every { getMoviesUseCase(true) }.returns(flowOf(ResultState.Success(latestMovies)))

        val vm = viewModel
        runCurrent()

        vm.setEvent(MovieListEvent.Refresh)
        advanceUntilIdle()

        assertThat(vm.viewState.value.data).isEqualTo(latestMovies.toUi())
    }

    @Test
    fun shouldNavigateToMovieDetailsWhenShowMovieDetailsEventIsTriggered() = runTest {
        // init starts the initial load, so the use case needs a stub even here.
        every { getMoviesUseCase(any()) }.returns(emptyFlow())

        // When
        viewModel.setEvent(MovieListEvent.ShowMovieDetails(movieId = 123))

        // Assert
        viewModel.effect.test {
            val effect = awaitItem()
            assertThat(effect).isEqualTo(MovieListSideEffect.NavigateToMovieDetails(123))

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun provideMovies(): List<Movie> = listOf(
        DomainTestDoubleFactory.provideMovieModel(),
        DomainTestDoubleFactory.provideMovieModel(),
    )

    private fun List<Movie>.toUi() = map { uiMapper.fromDomainToUi(it) }

    // Suspends between Loading and Success so the loading state is observable;
    // without the delay StateFlow conflates the two.
    private fun delayedSuccess(movies: List<Movie>) = flow {
        emit(ResultState.Loading)
        delay(1_000)
        emit(ResultState.Success(movies))
    }
}
