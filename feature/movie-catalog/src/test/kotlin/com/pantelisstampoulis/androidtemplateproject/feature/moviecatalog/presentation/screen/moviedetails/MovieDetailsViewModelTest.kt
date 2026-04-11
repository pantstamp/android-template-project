package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviedetails

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetMovieUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetWatchedMovieUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.RateMovieUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.SaveWatchedMovieUseCase
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper.MovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.model.error.ErrorModel
import com.pantelisstampoulis.androidtemplateproject.test.doubles.model.DomainTestDoubleFactory
import io.mockative.Mock
import io.mockative.any
import io.mockative.every
import io.mockative.mock
import io.mockative.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
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

class MovieDetailsViewModelTest : KoinTest {

    @Mock
    private val getMovieUseCase = mock(GetMovieUseCase::class)

    @Mock
    private val rateMovieUseCase = mock(RateMovieUseCase::class)

    @Mock
    private val saveWatchedMovieUseCase = mock(SaveWatchedMovieUseCase::class)

    @Mock
    private val getWatchedMovieUseCase = mock(GetWatchedMovieUseCase::class)

    private val uiMapper: MovieUiMapper by inject()

    private val viewModel: MovieDetailsViewModel by inject()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val mockModule = module {
            single { getMovieUseCase }
            single { rateMovieUseCase }
            single { saveWatchedMovieUseCase }
            single { getWatchedMovieUseCase }
            single { MovieUiMapper() }
            single { MovieDetailsViewModel(get(), get(), get(), get(), get()) }
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
    fun shouldEmitLoadingStateWhenFetchingMovieDetailsStarts() = runTest {
        every { getMovieUseCase(any()) }
            .returns(flow { emit(ResultState.Loading) })
        every { getWatchedMovieUseCase(any()) }
            .returns(flowOf(ResultState.Error(ErrorModel.NotFound())))

        viewModel.setEvent(MovieDetailsEvent.Init(movieId = 123))

        advanceUntilIdle()

        viewModel.viewState.test {
            val firstItem = awaitItem()
            assertThat(firstItem.isLoading).isTrue()
            assertThat(firstItem.data).isNull()
            cancelAndIgnoreRemainingEvents()
        }

        verify { getMovieUseCase(any()) }.wasInvoked()
    }

    @Test
    fun shouldEmitSuccessStateWhenFetchingMovieDetails() = runTest {
        val domainMovie = DomainTestDoubleFactory.provideMovieModel()
        val uiMovie = uiMapper.fromDomainToUi(domainMovie)

        every { getMovieUseCase(any()) }
            .returns(flowOf(ResultState.Success(domainMovie)))
        every { getWatchedMovieUseCase(any()) }
            .returns(flowOf(ResultState.Error(ErrorModel.NotFound())))

        viewModel.setEvent(MovieDetailsEvent.Init(movieId = 123))

        advanceUntilIdle()

        viewModel.viewState.test {
            val firstItem = awaitItem()
            assertThat(firstItem.isLoading).isFalse()
            assertThat(firstItem.errorMessage).isNull()
            assertThat(firstItem.data).isEqualTo(uiMovie)
            cancelAndIgnoreRemainingEvents()
        }

        verify { getMovieUseCase(any()) }.wasInvoked()
    }

    @Test
    fun shouldEmitErrorStateWhenFetchingMovieDetailsFails() = runTest {
        val errorMessage = "Failed to load movie details"
        val errorState = ErrorModel.NotFound(errorMessage)

        every { getMovieUseCase(any()) }
            .returns(flowOf(ResultState.Error(errorState)))
        every { getWatchedMovieUseCase(any()) }
            .returns(flowOf(ResultState.Error(ErrorModel.NotFound())))

        viewModel.setEvent(MovieDetailsEvent.Init(movieId = 123))

        advanceUntilIdle()

        viewModel.viewState.test {
            val firstItem = awaitItem()
            assertThat(firstItem.isLoading).isFalse()
            assertThat(firstItem.errorMessage).isEqualTo(errorMessage)
            assertThat(firstItem.data).isNull()
            cancelAndIgnoreRemainingEvents()
        }

        verify { getMovieUseCase(any()) }.wasInvoked()
    }

    @Test
    fun shouldSetUserRatingWhenMovieAlreadyRated() = runTest {
        val watchedMovie = DomainTestDoubleFactory.provideWatchedMovieModel()

        every { getMovieUseCase(any()) }
            .returns(flowOf(ResultState.Error(ErrorModel.NotFound())))
        every { getWatchedMovieUseCase(any()) }
            .returns(flowOf(ResultState.Success(watchedMovie)))

        viewModel.setEvent(MovieDetailsEvent.Init(movieId = watchedMovie.movieId))

        advanceUntilIdle()

        viewModel.viewState.test {
            val state = awaitItem()
            assertThat(state.userRating).isEqualTo(watchedMovie.userRating)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldLeaveUserRatingNullWhenMovieNotRated() = runTest {
        every { getMovieUseCase(any()) }
            .returns(flowOf(ResultState.Error(ErrorModel.NotFound())))
        every { getWatchedMovieUseCase(any()) }
            .returns(flowOf(ResultState.Error(ErrorModel.NotFound())))

        viewModel.setEvent(MovieDetailsEvent.Init(movieId = 123))

        advanceUntilIdle()

        viewModel.viewState.test {
            val state = awaitItem()
            assertThat(state.userRating).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldSaveWatchedMovieAndEmitSnackbarWhenRatingFullySucceeds() = runTest {
        val domainMovie = DomainTestDoubleFactory.provideMovieModel()

        // Load movie into state first
        every { getMovieUseCase(any()) }
            .returns(flowOf(ResultState.Success(domainMovie)))
        every { getWatchedMovieUseCase(any()) }
            .returns(flowOf(ResultState.Error(ErrorModel.NotFound())))
        viewModel.setEvent(MovieDetailsEvent.Init(movieId = domainMovie.id))
        advanceUntilIdle()

        // Rate the movie
        every { rateMovieUseCase(any()) }.returns(flowOf(ResultState.Success(Unit)))
        every { saveWatchedMovieUseCase(any()) }.returns(flowOf(ResultState.Success(Unit)))

        viewModel.setEvent(MovieDetailsEvent.RateMovie(movieId = domainMovie.id, rating = 8f))

        viewModel.effect.test {
            val effect = awaitItem()
            assertThat(effect).isEqualTo(MovieDetailsSideEffect.ShowSnackbar("Rating saved"))
            cancelAndIgnoreRemainingEvents()
        }

        advanceUntilIdle()

        viewModel.viewState.test {
            val state = awaitItem()
            assertThat(state.userRating).isEqualTo(8)
            assertThat(state.isRatingInProgress).isFalse()
            cancelAndIgnoreRemainingEvents()
        }

        verify { rateMovieUseCase(any()) }.wasInvoked()
        verify { saveWatchedMovieUseCase(any()) }.wasInvoked()
    }

    @Test
    fun shouldEmitErrorSnackbarWhenNetworkRatingFails() = runTest {
        every { getMovieUseCase(any()) }
            .returns(flowOf(ResultState.Error(ErrorModel.NotFound())))
        every { getWatchedMovieUseCase(any()) }
            .returns(flowOf(ResultState.Error(ErrorModel.NotFound())))

        every { rateMovieUseCase(any()) }
            .returns(flowOf(ResultState.Error(ErrorModel.ServerError("Network error"))))

        viewModel.setEvent(MovieDetailsEvent.RateMovie(movieId = 123, rating = 5f))

        viewModel.effect.test {
            val effect = awaitItem()
            assertThat(effect).isEqualTo(
                MovieDetailsSideEffect.ShowSnackbar("Something went wrong. Please try again."),
            )
            cancelAndIgnoreRemainingEvents()
        }

        advanceUntilIdle()

        viewModel.viewState.test {
            val state = awaitItem()
            assertThat(state.userRating).isNull()
            assertThat(state.isRatingInProgress).isFalse()
            cancelAndIgnoreRemainingEvents()
        }

        verify { rateMovieUseCase(any()) }.wasInvoked()
    }

    @Test
    fun shouldEmitErrorSnackbarWhenLocalSaveFails() = runTest {
        val domainMovie = DomainTestDoubleFactory.provideMovieModel()

        every { getMovieUseCase(any()) }
            .returns(flowOf(ResultState.Success(domainMovie)))
        every { getWatchedMovieUseCase(any()) }
            .returns(flowOf(ResultState.Error(ErrorModel.NotFound())))
        viewModel.setEvent(MovieDetailsEvent.Init(movieId = domainMovie.id))
        advanceUntilIdle()

        every { rateMovieUseCase(any()) }.returns(flowOf(ResultState.Success(Unit)))
        every { saveWatchedMovieUseCase(any()) }
            .returns(flowOf(ResultState.Error(ErrorModel.Unknown("db error"))))

        viewModel.setEvent(MovieDetailsEvent.RateMovie(movieId = domainMovie.id, rating = 5f))

        viewModel.effect.test {
            val effect = awaitItem()
            assertThat(effect).isEqualTo(
                MovieDetailsSideEffect.ShowSnackbar("Something went wrong. Please try again."),
            )
            cancelAndIgnoreRemainingEvents()
        }

        advanceUntilIdle()

        viewModel.viewState.test {
            val state = awaitItem()
            assertThat(state.userRating).isNull()
            assertThat(state.isRatingInProgress).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
