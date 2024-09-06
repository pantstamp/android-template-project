package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviedetails

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetMovieUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.RateMovieUseCase
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

    private val uiMapper: MovieUiMapper by inject()

    // Inject the ViewModel
    private val viewModel: MovieDetailsViewModel by inject()

    // Use TestDispatcher for coroutines testing
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Configure Koin for dependency injection
        val mockModule = module {
            single { getMovieUseCase }
            single { rateMovieUseCase }
            single { MovieUiMapper() }
            single { MovieDetailsViewModel(get(), get(), get()) }
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
            .returns(
                flow {
                    emit(ResultState.Loading) // Emit loading state
                },
            )

        viewModel.setEvent(MovieDetailsEvent.Init(movieId = 123))

        advanceUntilIdle()

        viewModel.viewState.test {
            val firstItem = awaitItem()
            assertThat(firstItem.isLoading).isTrue() // Loading state should be emitted first
            assertThat(firstItem.data).isNull() // No data yet, still loading
            cancelAndIgnoreRemainingEvents()
        }

        verify { getMovieUseCase(any()) }.wasInvoked()
    }

    @Test
    fun shouldEmitSuccessStateWhenFetchingMovieDetails() = runTest {
        val domainMovie = DomainTestDoubleFactory.provideMovieModel()
        val uiMovie = uiMapper.fromDomainToUi(domainMovie)

        every { getMovieUseCase(any()) }
            .returns(
                flow {
                    emit(ResultState.Success(domainMovie)) // Emit success state
                },
            )

        viewModel.setEvent(MovieDetailsEvent.Init(movieId = 123))

        advanceUntilIdle()

        viewModel.viewState.test {
            val firstItem = awaitItem()
            assertThat(firstItem.isLoading).isFalse() // Loading should be done
            assertThat(firstItem.errorMessage).isNull() // No error
            assertThat(firstItem.data).isEqualTo(uiMovie) // Data should be the mapped UI model
            cancelAndIgnoreRemainingEvents()
        }

        verify { getMovieUseCase(any()) }.wasInvoked()
    }

    @Test
    fun shouldEmitErrorStateWhenFetchingMovieDetailsFails() = runTest {
        val errorMessage = "Failed to load movie details"
        val errorState = ErrorModel.NotFound(errorMessage)

        every { getMovieUseCase(any()) }
            .returns(flowOf(ResultState.Error(errorState))) // Emit error state

        viewModel.setEvent(MovieDetailsEvent.Init(movieId = 123))

        advanceUntilIdle()

        viewModel.viewState.test {
            val firstItem = awaitItem()
            assertThat(firstItem.isLoading).isFalse() // Loading should be false
            assertThat(firstItem.errorMessage).isEqualTo(errorMessage) // Error message should be set
            assertThat(firstItem.data).isNull() // No data due to error
            cancelAndIgnoreRemainingEvents()
        }

        verify { getMovieUseCase(any()) }.wasInvoked()
    }

    @Test
    fun shouldEmitSuccessStateWhenRatingMovie() = runTest {
        every { rateMovieUseCase(any()) }
            .returns(flowOf(ResultState.Success(Unit))) // Emit success for rating

        viewModel.setEvent(MovieDetailsEvent.RateMovie(movieId = 123, rating = 5F))

        viewModel.effect.test {
            val effect = awaitItem()
            assertThat(effect).isEqualTo(MovieDetailsSideEffect.ShowToast("Movie rated successfully"))
            cancelAndIgnoreRemainingEvents()
        }

        verify { rateMovieUseCase(any()) }.wasInvoked()
    }

    @Test
    fun shouldEmitErrorStateWhenRatingMovieFails() = runTest {
        val errorMessage = "Rating failed"
        val errorState = ErrorModel.ServerError(errorMessage)

        every { rateMovieUseCase(any()) }
            .returns(flowOf(ResultState.Error(errorState))) // Emit error for rating

        viewModel.setEvent(MovieDetailsEvent.RateMovie(movieId = 123, rating = 5F))

        viewModel.effect.test {
            val effect = awaitItem()
            assertThat(effect).isEqualTo(MovieDetailsSideEffect.ShowToast("Error while rating Movie: $errorMessage"))
            cancelAndIgnoreRemainingEvents()
        }

        verify { rateMovieUseCase(any()) }.wasInvoked()
    }
}
