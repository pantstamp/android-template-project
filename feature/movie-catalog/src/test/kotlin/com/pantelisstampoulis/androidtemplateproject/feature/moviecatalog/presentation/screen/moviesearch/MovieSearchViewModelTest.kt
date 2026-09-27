@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviesearch

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.SearchMoviesUseCase
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.error.LoadError
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper.MovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.MovieUiModel
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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

class MovieSearchViewModelTest : KoinTest {

    @Mock
    private val searchMoviesUseCase = mock(SearchMoviesUseCase::class)

    private val uiMapper: MovieUiMapper by inject()

    private val viewModel: MovieSearchViewModel by inject()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val mockModule = module {
            single { searchMoviesUseCase }
            single { MovieUiMapper() }
            single { MovieSearchViewModel(get(), get()) }
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

    // region Activation and debounce

    @Test
    fun shouldStartInactiveAndNotSearch() = runTest {
        val vm = viewModel
        advanceUntilIdle()

        assertThat(vm.viewState.value).isEqualTo(MovieSearchUiState())
        assertThat(vm.viewState.value.isActive).isFalse()
        verify { searchMoviesUseCase(any()) }.wasNotInvoked()
    }

    @Test
    fun shouldStayInactiveForOneCharacter() = runTest {
        val vm = viewModel
        type(vm, "m")
        advanceTimeBy(1_000)
        runCurrent()

        assertThat(vm.viewState.value.isActive).isFalse()
        verify { searchMoviesUseCase(any()) }.wasNotInvoked()
    }

    @Test
    fun shouldStayInactiveForOneCharacterSurroundedBySpaces() = runTest {
        val vm = viewModel
        type(vm, "  m ")
        advanceTimeBy(1_000)
        runCurrent()

        assertThat(vm.viewState.value.isActive).isFalse()
        verify { searchMoviesUseCase(any()) }.wasNotInvoked()
    }

    @Test
    fun shouldActivateImmediatelyButSearchOnlyAfterDebounce() = runTest {
        every { searchMoviesUseCase("ma") }.returns(success(provideMovies()))

        val vm = viewModel
        type(vm, "ma")

        assertThat(vm.viewState.value.activeQuery).isEqualTo("ma")
        assertThat(vm.viewState.value.isSearching).isTrue()

        advanceTimeBy(299)
        runCurrent()
        verify { searchMoviesUseCase(any()) }.wasNotInvoked()

        advanceTimeBy(2)
        runCurrent()
        verify { searchMoviesUseCase("ma") }.wasInvoked(exactly = once)
    }

    @Test
    fun shouldSendTrimmedQuery() = runTest {
        every { searchMoviesUseCase("matrix") }.returns(success(provideMovies()))

        val vm = viewModel
        type(vm, "  matrix ")
        advanceUntilIdle()

        assertThat(vm.viewState.value.activeQuery).isEqualTo("matrix")
        verify { searchMoviesUseCase("matrix") }.wasInvoked(exactly = once)
    }

    @Test
    fun shouldSearchOnceForFastTyping() = runTest {
        every { searchMoviesUseCase("matrix") }.returns(success(provideMovies()))

        val vm = viewModel
        listOf("ma", "mat", "matr", "matri", "matrix").forEach { text ->
            type(vm, text)
            advanceTimeBy(100)
        }
        advanceUntilIdle()

        // Only "matrix" is stubbed: a search for any intermediate text would throw.
        verify { searchMoviesUseCase("matrix") }.wasInvoked(exactly = once)
    }

    @Test
    fun shouldNotSearchAgainWhenOnlySurroundingSpacesChange() = runTest {
        every { searchMoviesUseCase("matrix") }.returns(success(provideMovies()))

        val vm = viewModel
        search(vm, "matrix")
        type(vm, " matrix ")
        advanceUntilIdle()

        verify { searchMoviesUseCase(any()) }.wasInvoked(exactly = once)
    }

    @Test
    fun shouldNotSearchAgainWhenSameTextIsResent() = runTest {
        every { searchMoviesUseCase("matrix") }.returns(success(provideMovies()))

        val vm = viewModel
        search(vm, "matrix")
        val stateBefore = vm.viewState.value

        // What the screen sends after rotation or a return from the Watched tab.
        type(vm, "matrix")
        advanceUntilIdle()

        verify { searchMoviesUseCase(any()) }.wasInvoked(exactly = once)
        assertThat(vm.viewState.value).isEqualTo(stateBefore)
    }

    // endregion

    // region Results and ordering

    @Test
    fun shouldShowResultsForQuery() = runTest {
        val movies = provideMovies()
        every { searchMoviesUseCase("matrix") }.returns(success(movies))

        val vm = viewModel
        search(vm, "matrix")

        val state = vm.viewState.value
        assertThat(state.results).containsExactlyElementsIn(movies.toUi()).inOrder()
        assertThat(state.resultsQuery).isEqualTo("matrix")
        assertThat(state.isSearching).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun shouldShowEmptyResultsAsSuccess() = runTest {
        every { searchMoviesUseCase("zzzzqqq") }.returns(success(emptyList()))

        val vm = viewModel
        search(vm, "zzzzqqq")

        val state = vm.viewState.value
        assertThat(state.results).isNotNull()
        assertThat(state.results).isEmpty()
        assertThat(state.resultsQuery).isEqualTo("zzzzqqq")
        assertThat(state.error).isNull()
    }

    @Test
    fun shouldDiscardResultsOfSupersededQuery() = runTest {
        val staleMovies = provideMovies()
        val latestMovies = provideMovies()
        every { searchMoviesUseCase("mat") }.returns(success(staleMovies, delayMillis = 1_000))
        every { searchMoviesUseCase("matrix") }.returns(success(latestMovies, delayMillis = 10))

        val vm = viewModel
        val seenResults = mutableListOf<List<MovieUiModel>?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.viewState.collect { seenResults += it.results }
        }

        type(vm, "mat")
        advanceTimeBy(301) // the "mat" search is now running
        runCurrent()
        type(vm, "matrix")
        advanceTimeBy(2_000) // past both deadlines
        runCurrent()

        assertThat(vm.viewState.value.results).containsExactlyElementsIn(latestMovies.toUi()).inOrder()
        assertThat(vm.viewState.value.resultsQuery).isEqualTo("matrix")
        assertThat(seenResults).doesNotContain(staleMovies.toUi())
    }

    @Test
    fun shouldKeepPreviousResultsWhileNextQueryIsPending() = runTest {
        val matMovies = provideMovies()
        every { searchMoviesUseCase("mat") }.returns(success(matMovies))
        every { searchMoviesUseCase("matrix") }.returns(success(provideMovies(), delayMillis = 1_000))

        val vm = viewModel
        search(vm, "mat")
        type(vm, "matrix")

        val state = vm.viewState.value
        assertThat(state.results).containsExactlyElementsIn(matMovies.toUi()).inOrder()
        assertThat(state.resultsQuery).isEqualTo("mat")
        assertThat(state.activeQuery).isEqualTo("matrix")
        assertThat(state.isSearching).isTrue()
    }

    @Test
    fun shouldShowSpinnerRatherThanPreviousErrorWhileNextQueryIsPending() = runTest {
        every { searchMoviesUseCase("mat") }.returns(failure(DomainError.NoNetworkConnection()))
        every { searchMoviesUseCase("matrix") }.returns(success(provideMovies()))

        val vm = viewModel
        search(vm, "mat")
        assertThat(vm.viewState.value.error).isEqualTo(LoadError.Offline)

        type(vm, "matrix")

        assertThat(vm.viewState.value.error).isNull()
        assertThat(vm.viewState.value.isSearching).isTrue()
    }

    @Test
    fun shouldEmitScrollToTopWhenResultsForNewQueryArrive() = runTest {
        every { searchMoviesUseCase("mat") }.returns(success(provideMovies()))
        every { searchMoviesUseCase("matrix") }.returns(success(provideMovies()))

        val vm = viewModel
        search(vm, "mat")
        search(vm, "matrix")

        vm.effect.test {
            assertThat(awaitItem()).isEqualTo(MovieSearchSideEffect.ScrollResultsToTop("mat"))
            assertThat(awaitItem()).isEqualTo(MovieSearchSideEffect.ScrollResultsToTop("matrix"))
            expectNoEvents()
        }
    }

    @Test
    fun shouldNotEmitScrollToTopWhenSameQueryIsRefreshed() = runTest {
        every { searchMoviesUseCase("matrix") }.returns(success(provideMovies()))

        val vm = viewModel
        search(vm, "matrix")

        vm.effect.test {
            assertThat(awaitItem()).isEqualTo(MovieSearchSideEffect.ScrollResultsToTop("matrix"))
            vm.setEvent(MovieSearchEvent.Refresh)
            advanceUntilIdle()
            expectNoEvents()
        }
        verify { searchMoviesUseCase("matrix") }.wasInvoked(exactly = 2)
    }

    @Test
    fun shouldNotEmitScrollToTopWhenSameQueryIsRetried() = runTest {
        every { searchMoviesUseCase("matrix") }.returns(success(provideMovies()))

        val vm = viewModel
        search(vm, "matrix")

        vm.effect.test {
            assertThat(awaitItem()).isEqualTo(MovieSearchSideEffect.ScrollResultsToTop("matrix"))
            vm.setEvent(MovieSearchEvent.Retry)
            advanceUntilIdle()
            expectNoEvents()
        }
        verify { searchMoviesUseCase("matrix") }.wasInvoked(exactly = 2)
    }

    // endregion

    // region Clearing

    @Test
    fun shouldReturnToInactiveImmediatelyWhenCleared() = runTest {
        every { searchMoviesUseCase("matrix") }.returns(success(provideMovies()))

        val vm = viewModel
        search(vm, "matrix")
        type(vm, "")

        assertThat(vm.viewState.value).isEqualTo(MovieSearchUiState())
    }

    @Test
    fun shouldReturnToInactiveWhenQueryDropsBelowMinimum() = runTest {
        every { searchMoviesUseCase("matrix") }.returns(success(provideMovies()))

        val vm = viewModel
        search(vm, "matrix")
        type(vm, "m")

        assertThat(vm.viewState.value).isEqualTo(MovieSearchUiState())
    }

    @Test
    fun shouldCancelInFlightSearchWhenCleared() = runTest {
        every { searchMoviesUseCase("matrix") }.returns(success(provideMovies(), delayMillis = 1_000))

        val vm = viewModel
        type(vm, "matrix")
        advanceTimeBy(301) // the search is now running
        runCurrent()
        type(vm, "")
        advanceUntilIdle()

        assertThat(vm.viewState.value).isEqualTo(MovieSearchUiState())
    }

    @Test
    fun shouldSearchAgainWhenSameQueryIsTypedAfterClearing() = runTest {
        every { searchMoviesUseCase("matrix") }.returns(success(provideMovies()))

        val vm = viewModel
        search(vm, "matrix")
        type(vm, "")
        search(vm, "matrix")

        verify { searchMoviesUseCase("matrix") }.wasInvoked(exactly = 2)
        assertThat(vm.viewState.value.resultsQuery).isEqualTo("matrix")
    }

    @Test
    fun shouldSearchAgainWhenClearedAndRetypedBeforeThePipelineRuns() = runTest {
        val movies = provideMovies()
        every { searchMoviesUseCase("matrix") }.returns(success(movies))

        val vm = viewModel
        search(vm, "matrix")

        // Both changes are handled before the search pipeline gets to run.
        vm.setEvent(MovieSearchEvent.QueryChanged(""))
        vm.setEvent(MovieSearchEvent.QueryChanged("matrix"))
        advanceUntilIdle()

        verify { searchMoviesUseCase("matrix") }.wasInvoked(exactly = 2)
        val state = vm.viewState.value
        assertThat(state.isSearching).isFalse()
        assertThat(state.results).containsExactlyElementsIn(movies.toUi()).inOrder()
    }

    // endregion

    // region Errors, retry and refresh

    @Test
    fun shouldShowOfflineErrorWhenSearchFailsWithNoNetwork() = runTest {
        every { searchMoviesUseCase("matrix") }.returns(failure(DomainError.NoNetworkConnection()))

        val vm = viewModel
        search(vm, "matrix")

        val state = vm.viewState.value
        assertThat(state.error).isEqualTo(LoadError.Offline)
        assertThat(state.results).isNull()
        assertThat(state.isSearching).isFalse()
    }

    @Test
    fun shouldShowGenericErrorWhenSearchFailsOtherwise() = runTest {
        every { searchMoviesUseCase("matrix") }.returns(failure(DomainError.ServerError()))

        val vm = viewModel
        search(vm, "matrix")

        assertThat(vm.viewState.value.error).isEqualTo(LoadError.Generic)
    }

    @Test
    fun shouldReplacePreviousResultsWithErrorWhenNewQueryFails() = runTest {
        every { searchMoviesUseCase("mat") }.returns(success(provideMovies()))
        every { searchMoviesUseCase("matrix") }.returns(failure(DomainError.NoNetworkConnection()))

        val vm = viewModel
        search(vm, "mat")
        search(vm, "matrix")

        val state = vm.viewState.value
        assertThat(state.results).isNull()
        assertThat(state.resultsQuery).isNull()
        assertThat(state.error).isEqualTo(LoadError.Offline)
        vm.effect.test {
            assertThat(awaitItem()).isEqualTo(MovieSearchSideEffect.ScrollResultsToTop("mat"))
            expectNoEvents() // no RefreshFailed: this was a new query, not a refresh
        }
    }

    @Test
    fun shouldRerunCurrentQueryOnRetryWithoutDebounce() = runTest {
        val movies = provideMovies()
        every { searchMoviesUseCase("matrix") }
            .returns(inSequence(failure(DomainError.NoNetworkConnection()), success(movies)))

        val vm = viewModel
        search(vm, "matrix")
        assertThat(vm.viewState.value.error).isEqualTo(LoadError.Offline)

        vm.setEvent(MovieSearchEvent.Retry)
        runCurrent() // no time advance: a retry is not debounced

        verify { searchMoviesUseCase("matrix") }.wasInvoked(exactly = 2)
        val state = vm.viewState.value
        assertThat(state.error).isNull()
        assertThat(state.results).containsExactlyElementsIn(movies.toUi()).inOrder()
    }

    @Test
    fun shouldIgnoreRetryAndRefreshWhenInactive() = runTest {
        val vm = viewModel
        vm.setEvent(MovieSearchEvent.Retry)
        vm.setEvent(MovieSearchEvent.Refresh)
        advanceUntilIdle()

        assertThat(vm.viewState.value).isEqualTo(MovieSearchUiState())
        verify { searchMoviesUseCase(any()) }.wasNotInvoked()
    }

    @Test
    fun shouldRerunCurrentQueryOnRefresh() = runTest {
        val oldMovies = provideMovies()
        val newMovies = provideMovies()
        every { searchMoviesUseCase("matrix") }
            .returns(inSequence(success(oldMovies), success(newMovies, delayMillis = 100)))

        val vm = viewModel
        search(vm, "matrix")

        vm.setEvent(MovieSearchEvent.Refresh)
        runCurrent()
        assertThat(vm.viewState.value.isRefreshing).isTrue()
        assertThat(vm.viewState.value.results).containsExactlyElementsIn(oldMovies.toUi()).inOrder()

        advanceUntilIdle()
        assertThat(vm.viewState.value.isRefreshing).isFalse()
        assertThat(vm.viewState.value.results).containsExactlyElementsIn(newMovies.toUi()).inOrder()
    }

    @Test
    fun shouldKeepResultsAndEmitRefreshFailedWhenRefreshFails() = runTest {
        val movies = provideMovies()
        every { searchMoviesUseCase("matrix") }
            .returns(inSequence(success(movies), failure(DomainError.NoNetworkConnection())))

        val vm = viewModel
        search(vm, "matrix")
        vm.setEvent(MovieSearchEvent.Refresh)
        advanceUntilIdle()

        val state = vm.viewState.value
        assertThat(state.results).containsExactlyElementsIn(movies.toUi()).inOrder()
        assertThat(state.isRefreshing).isFalse()
        assertThat(state.isSearching).isFalse()
        assertThat(state.error).isNull()
        vm.effect.test {
            assertThat(awaitItem()).isEqualTo(MovieSearchSideEffect.ScrollResultsToTop("matrix"))
            assertThat(awaitItem()).isEqualTo(MovieSearchSideEffect.RefreshFailed(LoadError.Offline))
        }
    }

    @Test
    fun shouldTreatRefreshWithoutResultsAsRetry() = runTest {
        every { searchMoviesUseCase("matrix") }
            .returns(inSequence(failure(DomainError.ServerError()), success(provideMovies(), delayMillis = 100)))

        val vm = viewModel
        search(vm, "matrix")
        vm.setEvent(MovieSearchEvent.Refresh)
        runCurrent()

        val state = vm.viewState.value
        assertThat(state.error).isNull()
        assertThat(state.isSearching).isTrue()
        assertThat(state.isRefreshing).isFalse()
        verify { searchMoviesUseCase("matrix") }.wasInvoked(exactly = 2)
    }

    @Test
    fun shouldResetRefreshingWhenNewQueryInterruptsRefresh() = runTest {
        every { searchMoviesUseCase("matrix") }
            .returns(inSequence(success(provideMovies()), success(provideMovies(), delayMillis = 1_000)))
        every { searchMoviesUseCase("other") }.returns(success(provideMovies()))

        val vm = viewModel
        search(vm, "matrix")
        vm.setEvent(MovieSearchEvent.Refresh)
        runCurrent()
        assertThat(vm.viewState.value.isRefreshing).isTrue()

        type(vm, "other")

        assertThat(vm.viewState.value.isRefreshing).isFalse()
        assertThat(vm.viewState.value.isSearching).isTrue()
    }

    // endregion

    @Test
    fun shouldEmitNavigateToMovieDetailsOnShowMovieDetails() = runTest {
        val vm = viewModel
        vm.setEvent(MovieSearchEvent.ShowMovieDetails(movieId = 603))
        advanceUntilIdle()

        vm.effect.test {
            assertThat(awaitItem()).isEqualTo(MovieSearchSideEffect.NavigateToMovieDetails(603))
        }
    }

    private fun TestScope.type(vm: MovieSearchViewModel, text: String) {
        vm.setEvent(MovieSearchEvent.QueryChanged(text))
        runCurrent()
    }

    /** Types [text] and lets the debounce and the search complete. */
    private fun TestScope.search(vm: MovieSearchViewModel, text: String) {
        type(vm, text)
        advanceUntilIdle()
    }

    private fun success(movies: List<Movie>, delayMillis: Long = 0): Flow<ResultState<List<Movie>>> = flow {
        emit(ResultState.Loading)
        delay(delayMillis)
        emit(ResultState.Success(movies))
    }

    private fun failure(error: DomainError): Flow<ResultState<List<Movie>>> = flow {
        emit(ResultState.Loading)
        emit(ResultState.Error(error))
    }

    /** A cold flow that behaves like [flows] in turn on each collection, repeating the last. */
    private fun inSequence(vararg flows: Flow<ResultState<List<Movie>>>): Flow<ResultState<List<Movie>>> {
        var calls = 0
        return flow {
            val index = minOf(calls, flows.lastIndex)
            calls++
            emitAll(flows[index])
        }
    }

    private fun provideMovies() = listOf(
        DomainTestDoubleFactory.provideMovieModel(),
        DomainTestDoubleFactory.provideMovieModel(),
    )

    private fun List<Movie>.toUi(): List<MovieUiModel> = map(uiMapper::fromDomainToUi)
}
