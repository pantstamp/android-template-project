package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetMoviesUseCase
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

class MovieListViewModelTest : KoinTest {

    @Mock
    private val getMoviesUseCase = mock(GetMoviesUseCase::class)

    private val uiMapper: MovieUiMapper by inject()

    // Inject the ViewModel
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
    fun shouldEmitLoadingStateWhenFetchingMoviesStarts() = runTest {
        every { getMoviesUseCase(any()) }
            .returns(
                flow {
                    emit(ResultState.Loading) // Emit loading state
                },
            )

        viewModel.setEvent(MovieListEvent.GetMovies(ignoreCache = false))

        advanceUntilIdle()

        // Then: verifying that the state emits loading first, followed by success
        viewModel.viewState.test {
            // First emission should indicate loading
            val firstItem = awaitItem()
            assertThat(firstItem.isLoading).isTrue() // Expecting the loading state
            assertThat(firstItem.data).isNull() // No data yet, just loading

            cancelAndIgnoreRemainingEvents()
        }

        // Verify that the use case was invoked with the correct argument
        verify { getMoviesUseCase(any()) }.wasInvoked()
    }

    @Test
    fun shouldEmitSuccessStateWhenFetchingMovies() = runTest {
        // Given
        val domainMovies = listOf(
            DomainTestDoubleFactory.provideMovieModel(),
            DomainTestDoubleFactory.provideMovieModel(),
        )
        val uiMovies = domainMovies.map { uiMapper.fromDomainToUi(it) }

        every { getMoviesUseCase(any()) }
            .returns(
                flow {
                    emit(ResultState.Success(domainMovies)) // Emit success state
                },
            )

        // When
        viewModel.setEvent(MovieListEvent.GetMovies(ignoreCache = false))

        // Ensure that all the coroutines and state emissions have been processed
        advanceUntilIdle()

        // Then
        viewModel.viewState.test {
            val firstItem = awaitItem()
            assertThat(firstItem.isLoading).isFalse() // Loading should be done
            assertThat(firstItem.errorMessage).isNull() // No error should be present
            assertThat(firstItem.data).isEqualTo(uiMovies) // Data should now be present

            cancelAndIgnoreRemainingEvents()
        }

        // Verify that the use case was invoked with the correct argument
        verify { getMoviesUseCase(any()) }.wasInvoked()
    }

    @Test
    fun shouldEmitErrorStateWhenFetchingMoviesFails() = runTest {
        // Given
        val errorMessage = "Server error"
        val errorState = ErrorModel.ServerError(errorMessage)

        every { getMoviesUseCase(any()) }.returns(flowOf(ResultState.Error(errorState)))

        // When
        viewModel.setEvent(MovieListEvent.GetMovies(ignoreCache = false))

        // Then
        viewModel.viewState.test {
            val firstItem = awaitItem()
            assertThat(firstItem.isLoading).isFalse()
            assertThat(firstItem.errorMessage).isNull()
            assertThat(firstItem.data).isNull()

            // Check side effect for showing toast
            viewModel.effect.test {
                val toastEffect = awaitItem()
                assertThat(toastEffect).isEqualTo(MovieListSideEffect.ShowToast(errorMessage))
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldNavigateToMovieDetailsWhenShowMovieDetailsEventIsTriggered() = runTest {
        // When
        viewModel.setEvent(MovieListEvent.ShowMovieDetails(movieId = 123))

        // Assert
        viewModel.effect.test {
            val effect = awaitItem()
            assertThat(effect).isEqualTo(MovieListSideEffect.NavigateToMovieDetails(123))

            cancelAndIgnoreRemainingEvents()
        }
    }
}
