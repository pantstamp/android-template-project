package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.watchedmovielist

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetWatchedMoviesUseCase
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper.WatchedMovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.model.error.ErrorModel
import com.pantelisstampoulis.androidtemplateproject.test.doubles.model.DomainTestDoubleFactory
import io.mockative.Mock
import io.mockative.any
import io.mockative.every
import io.mockative.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
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

class WatchedMovieListViewModelTest : KoinTest {

    @Mock
    private val getWatchedMoviesUseCase = mock(GetWatchedMoviesUseCase::class)

    private val uiMapper: WatchedMovieUiMapper by inject()

    private val viewModel: WatchedMovieListViewModel by inject()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val mockModule = module {
            single { getWatchedMoviesUseCase }
            single { WatchedMovieUiMapper() }
            single { WatchedMovieListViewModel(get(), get()) }
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
    fun shouldEmitMappedWatchedMoviesWhenFetchSucceeds() = runTest {
        val watchedMovies = listOf(
            DomainTestDoubleFactory.provideWatchedMovieModel(),
            DomainTestDoubleFactory.provideWatchedMovieModel(),
        )
        val expectedUiModels = watchedMovies.map { uiMapper.fromDomainToUi(it) }

        every { getWatchedMoviesUseCase(any()) }
            .returns(flowOf(ResultState.Success(watchedMovies)))

        // Trigger ViewModel creation (init {} launches the collection coroutine)
        // then run it to completion before collecting viewState.
        val vm = viewModel
        advanceUntilIdle()

        vm.viewState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.errorRes).isNull()
            assertThat(state.data).isEqualTo(expectedUiModels)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldEmitEmptyListWhenNoWatchedMovies() = runTest {
        every { getWatchedMoviesUseCase(any()) }
            .returns(flowOf(ResultState.Success(emptyList())))

        val vm = viewModel
        advanceUntilIdle()

        vm.viewState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.errorRes).isNull()
            assertThat(state.data).isNotNull()
            assertThat(state.data).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldEmitErrorMessageWhenFetchFails() = runTest {
        every { getWatchedMoviesUseCase(any()) }
            .returns(flowOf(ResultState.Error(ErrorModel.ServerError("Server error"))))

        val vm = viewModel
        advanceUntilIdle()

        vm.viewState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.errorRes).isNotNull()
            assertThat(state.data).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldEmitNavigateEffectWhenShowMovieDetailsEventTriggered() = runTest {
        every { getWatchedMoviesUseCase(any()) }.returns(emptyFlow())

        viewModel.setEvent(WatchedMovieListEvent.ShowMovieDetails(movieId = 42))

        viewModel.effect.test {
            val effect = awaitItem()
            assertThat(effect).isEqualTo(WatchedMovieListSideEffect.NavigateToMovieDetails(42))
            cancelAndIgnoreRemainingEvents()
        }
    }
}
