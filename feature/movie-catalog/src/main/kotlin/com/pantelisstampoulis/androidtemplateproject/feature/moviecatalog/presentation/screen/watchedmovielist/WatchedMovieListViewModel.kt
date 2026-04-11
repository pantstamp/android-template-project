package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.watchedmovielist

import com.pantelisstampoulis.androidtemplateproject.domain.onError
import com.pantelisstampoulis.androidtemplateproject.domain.onLoading
import com.pantelisstampoulis.androidtemplateproject.domain.onSuccess
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetWatchedMoviesUseCase
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper.WatchedMovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.WatchedMovieUiModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.MviViewModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

class WatchedMovieListViewModel(
    private val getWatchedMoviesUseCase: GetWatchedMoviesUseCase,
    private val mapper: WatchedMovieUiMapper,
) : MviViewModel<WatchedMovieListEvent, WatchedMovieListUiState, WatchedMovieListSideEffect>(
    initialState = WatchedMovieListUiState(),
) {

    override fun handleEvents(event: WatchedMovieListEvent) {
        when (event) {
            is WatchedMovieListEvent.GetWatchedMovies -> {
                viewModelScope.launch {
                    getWatchedMoviesUseCase(input = Unit).collect { resultState ->
                        resultState
                            .onLoading {
                                setState { copy(isLoading = true) }
                            }
                            .onSuccess {
                                setState {
                                    copy(
                                        isLoading = false,
                                        errorMessage = null,
                                        data = it.map { mapper.fromDomainToUi(it) }
                                            .toImmutableList(),
                                    )
                                }
                            }
                            .onError { error ->
                                setState {
                                    copy(
                                        isLoading = false,
                                        errorMessage = error.message ?: "An error occurred",
                                    )
                                }
                            }
                    }
                }
            }

            is WatchedMovieListEvent.ShowMovieDetails -> setEffect {
                WatchedMovieListSideEffect.NavigateToMovieDetails(event.movieId)
            }
        }
    }
}

data class WatchedMovieListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val data: ImmutableList<WatchedMovieUiModel>? = null,
) : UiState
