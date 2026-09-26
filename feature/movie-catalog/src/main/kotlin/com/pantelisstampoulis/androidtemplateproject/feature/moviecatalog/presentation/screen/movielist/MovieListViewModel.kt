package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist

import com.pantelisstampoulis.androidtemplateproject.domain.onError
import com.pantelisstampoulis.androidtemplateproject.domain.onLoading
import com.pantelisstampoulis.androidtemplateproject.domain.onSuccess
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetMoviesUseCase
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper.MovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.MovieUiModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.MviViewModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MovieListViewModel(
    private val getMoviesUseCase: GetMoviesUseCase,
    private val mapper: MovieUiMapper,
) : MviViewModel<MovieListEvent, MovieListUiState, MovieListSideEffect>(
    initialState = MovieListUiState(),
) {

    // Declared before init: property initialisers and init blocks run in declaration order.
    private var loadJob: Job? = null

    init {
        loadMovies(ignoreCache = false)
    }

    override fun handleEvents(event: MovieListEvent) {
        when (event) {
            MovieListEvent.Refresh -> loadMovies(ignoreCache = true)

            is MovieListEvent.ShowMovieDetails -> setEffect {
                MovieListSideEffect.NavigateToMovieDetails(event.movieId)
            }
        }
    }

    private fun loadMovies(ignoreCache: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getMoviesUseCase(input = ignoreCache).collect { resultState ->
                resultState
                    .onLoading {
                        setState {
                            // Movies on screen stay visible behind the pull indicator.
                            if (hasMovies) copy(isRefreshing = true) else copy(isLoading = true, error = null)
                        }
                    }
                    .onSuccess { movies ->
                        setState {
                            copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = null,
                                data = movies.map { mapper.fromDomainToUi(it) }.toImmutableList(),
                            )
                        }
                    }
                    .onError { domainError ->
                        val error = domainError.toMovieListError()
                        val hadMovies = viewState.value.hasMovies
                        setState {
                            copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = if (hadMovies) null else error,
                            )
                        }
                        if (hadMovies) {
                            setEffect { MovieListSideEffect.RefreshFailed(error) }
                        }
                    }
            }
        }
    }
}

data class MovieListUiState(
    // True initially because init starts the first load, so the first frame is not blank.
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    // Set only when there are no movies to show; a failed refresh over a list is a side effect.
    val error: MovieListError? = null,
    val data: ImmutableList<MovieUiModel>? = null,
) : UiState {
    val hasMovies: Boolean get() = !data.isNullOrEmpty()
}
