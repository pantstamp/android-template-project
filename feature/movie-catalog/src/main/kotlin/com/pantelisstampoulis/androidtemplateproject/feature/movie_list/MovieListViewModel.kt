package com.pantelisstampoulis.androidtemplateproject.feature.movie_list


import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetMoviesUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.onError
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.onLoading
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.onSuccess
import com.pantelisstampoulis.androidtemplateproject.feature.movie_list.mapper.MovieUiMapper
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
            is MovieListEvent.Init -> {
                viewModelScope.launch {
                    getMoviesUseCase(input = Unit).collect { useCaseState ->
                        useCaseState
                            .onLoading { setState { this.copy(isLoading = true) } }
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
                            .onError { throwable ->
                                setState {
                                    this.copy(
                                        isLoading = false,
                                        errorMessage = throwable.message,
                                    )
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
