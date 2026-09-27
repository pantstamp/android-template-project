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
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.error.LoadError
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.error.toLoadError
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper.MovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.MovieUiModel
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.MviViewModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.UiState
import kotlinx.coroutines.Job
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

    private var movieLoadJob: Job? = null

    override fun handleEvents(event: MovieDetailsEvent) {
        when (event) {
            is MovieDetailsEvent.Init -> {
                loadMovie(event.movieId)
                viewModelScope.launch {
                    getWatchedMovieUseCase(input = event.movieId).collect { resultState ->
                        resultState
                            .onSuccess { watchedMovie ->
                                // null = not rated yet, which is a normal outcome
                                setState { copy(userRating = watchedMovie?.userRating) }
                            }
                            .onError {
                                // a real failure; the rating simply stays unset
                            }
                    }
                }
            }

            is MovieDetailsEvent.Retry -> loadMovie(event.movieId)

            is MovieDetailsEvent.RateMovie -> {
                setState { copy(isRatingInProgress = true) }
                viewModelScope.launch {
                    rateMovieUseCase.invoke(
                        RateMovieUseCaseInput(event.movieId, event.rating),
                    ).collect { resultState ->
                        resultState
                            .onSuccess {
                                val movie = viewState.value.movie
                                if (movie == null) {
                                    setState { copy(isRatingInProgress = false) }
                                    setEffect { MovieDetailsSideEffect.RatingError }
                                    return@onSuccess
                                }
                                saveRating(movie = movie, rating = event.rating.toInt())
                            }
                            .onError {
                                setState { copy(isRatingInProgress = false) }
                                setEffect { MovieDetailsSideEffect.RatingError }
                            }
                    }
                }
            }
        }
    }

    private fun loadMovie(movieId: Int) {
        movieLoadJob?.cancel()
        movieLoadJob = viewModelScope.launch {
            getMovieUseCase(input = movieId).collect { resultState ->
                resultState
                    .onLoading { setState { copy(isLoading = true, error = null) } }
                    .onSuccess { movie ->
                        setState {
                            copy(
                                isLoading = false,
                                error = null,
                                movie = movie,
                                data = mapper.fromDomainToUi(movie),
                            )
                        }
                    }
                    .onError { domainError ->
                        setState { copy(isLoading = false, error = domainError.toLoadError()) }
                    }
            }
        }
    }

    private suspend fun saveRating(movie: Movie, rating: Int) {
        saveWatchedMovieUseCase.invoke(
            SaveWatchedMovieInput(
                movieId = movie.id,
                title = movie.title,
                posterUrl = movie.posterPath,
                overview = movie.overview,
                publicRating = movie.voteAverage,
                releaseDate = movie.releaseDate,
                userRating = rating,
            ),
        ).collect { saveResult ->
            saveResult
                .onSuccess {
                    setState { copy(userRating = rating, isRatingInProgress = false) }
                    setEffect { MovieDetailsSideEffect.RatingSaved }
                }
                .onError {
                    setState { copy(isRatingInProgress = false) }
                    setEffect { MovieDetailsSideEffect.RatingError }
                }
        }
    }
}

data class MovieDetailsUiState(
    val isLoading: Boolean = false,
    /** Set when the movie could not be loaded; the composable resolves the message. */
    val error: LoadError? = null,
    /**
     * The domain movie, kept so that domain operations use domain data. [data] is the same
     * movie formatted for display, and its [MovieUiModel.releaseYear] is a truncated year
     * that must never be persisted as a release date.
     */
    val movie: Movie? = null,
    val data: MovieUiModel? = null,
    val userRating: Int? = null,
    val isRatingInProgress: Boolean = false,
) : UiState
