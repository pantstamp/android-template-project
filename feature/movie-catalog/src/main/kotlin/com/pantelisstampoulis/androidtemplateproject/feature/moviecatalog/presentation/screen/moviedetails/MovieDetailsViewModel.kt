package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviedetails

import com.pantelisstampoulis.androidtemplateproject.domain.onError
import com.pantelisstampoulis.androidtemplateproject.domain.onLoading
import com.pantelisstampoulis.androidtemplateproject.domain.onSuccess
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetMovieUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetWatchedMovieUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.RateMovieUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.RateMovieUseCaseInput
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.SaveWatchedMovieInput
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.SaveWatchedMovieUseCase
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper.MovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.MovieUiModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.MviViewModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.UiState
import kotlinx.coroutines.launch

class MovieDetailsViewModel(
    private val getMovieUseCase: GetMovieUseCase,
    private val rateMovieUseCase: RateMovieUseCase,
    private val saveWatchedMovieUseCase: SaveWatchedMovieUseCase,
    private val getWatchedMovieUseCase: GetWatchedMovieUseCase,
    private val mapper: MovieUiMapper,
) : MviViewModel<MovieDetailsEvent, MovieDetailsUiState, MovieDetailsSideEffect>(
    initialState = MovieDetailsUiState(),
) {

    override fun handleEvents(event: MovieDetailsEvent) {
        when (event) {
            is MovieDetailsEvent.Init -> {
                viewModelScope.launch {
                    getMovieUseCase(input = event.movieId).collect { resultState ->
                        resultState
                            .onLoading { setState { this.copy(isLoading = true) } }
                            .onSuccess {
                                setState {
                                    this.copy(
                                        isLoading = false,
                                        errorMessage = null,
                                        data = mapper.fromDomainToUi(it),
                                    )
                                }
                            }
                            .onError { error ->
                                setState {
                                    this.copy(
                                        isLoading = false,
                                        errorMessage = error.message,
                                    )
                                }
                            }
                    }
                }
                viewModelScope.launch {
                    getWatchedMovieUseCase(input = event.movieId).collect { resultState ->
                        resultState
                            .onSuccess { watchedMovie ->
                                setState { copy(userRating = watchedMovie.userRating) }
                            }
                            .onError {
                                // NotFound = not rated yet, leave userRating as null
                            }
                    }
                }
            }

            is MovieDetailsEvent.RateMovie -> {
                setState { copy(isRatingInProgress = true) }
                viewModelScope.launch {
                    rateMovieUseCase.invoke(
                        RateMovieUseCaseInput(event.movieId, event.rating),
                    ).collect { resultState ->
                        resultState
                            .onSuccess {
                                val movie = viewState.value.data ?: return@onSuccess
                                saveWatchedMovieUseCase.invoke(
                                    SaveWatchedMovieInput(
                                        movieId = movie.id,
                                        title = movie.title,
                                        posterUrl = movie.posterPath,
                                        overview = movie.overview,
                                        publicRating = movie.voteAverage,
                                        releaseDate = movie.releaseYear,
                                        userRating = event.rating.toInt(),
                                    ),
                                ).collect { saveResult ->
                                    saveResult
                                        .onSuccess {
                                            setState {
                                                copy(
                                                    userRating = event.rating.toInt(),
                                                    isRatingInProgress = false,
                                                )
                                            }
                                            setEffect { MovieDetailsSideEffect.ShowSnackbar("Rating saved") }
                                        }
                                        .onError {
                                            setState { copy(isRatingInProgress = false) }
                                            setEffect {
                                                MovieDetailsSideEffect.ShowSnackbar(
                                                    "Something went wrong. Please try again.",
                                                )
                                            }
                                        }
                                }
                            }
                            .onError {
                                setState { copy(isRatingInProgress = false) }
                                setEffect {
                                    MovieDetailsSideEffect.ShowSnackbar(
                                        "Something went wrong. Please try again.",
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
}

data class MovieDetailsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val data: MovieUiModel? = null,
    val userRating: Int? = null,
    val isRatingInProgress: Boolean = false,
) : UiState
