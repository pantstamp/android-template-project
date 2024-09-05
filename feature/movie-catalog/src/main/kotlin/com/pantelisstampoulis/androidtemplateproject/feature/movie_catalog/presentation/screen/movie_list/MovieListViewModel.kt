package com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.presentation.screen.movie_list

import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetMoviesUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.onError
import com.pantelisstampoulis.androidtemplateproject.domain.onLoading
import com.pantelisstampoulis.androidtemplateproject.domain.onSuccess
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.presentation.ui_model.MovieUiModel
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.presentation.mapper.MovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.MviViewModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.UiState
import kotlinx.coroutines.launch
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class MovieListViewModel(
    private val getMoviesUseCase: GetMoviesUseCase,
    private val mapper: MovieUiMapper
) : MviViewModel<MovieListEvent, MovieListUiState, MovieListSideEffect>(
    initialState = MovieListUiState(),
) {

    override fun handleEvents(event: MovieListEvent) {
        when (event) {
            is MovieListEvent.GetMovies -> {
                viewModelScope.launch {
                    getMoviesUseCase(input = event.ignoreCache).collect { resultState ->
                        resultState
                            .onLoading {
                                setState { this.copy(isLoading = true) }
                            }
                            .onSuccess {
                                setState {
                                    this.copy(
                                        isLoading = false,
                                        errorMessage = null,
                                        data = it.map { mapper.fromDomainToUi(it) }
                                            .toImmutableList(),
                                    )
                                }
                            }
                            .onError { error ->
                                setState {
                                    this.copy(
                                        isLoading = false
                                    )
                                }
                                setEffect {
                                    val errorMessage = error.message ?: "An error occurred"
                                    MovieListSideEffect.ShowToast(errorMessage)
                                }
                            }
                    }
                }
            }
            is MovieListEvent.ShowMovieDetails -> setEffect {
                MovieListSideEffect.NavigateToMovieDetails(event.movieId)
            }
        }
    }
}

data class MovieListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val data: ImmutableList<MovieUiModel>? = null,
) : UiState
