package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.watchedmovielist

import androidx.annotation.StringRes
import com.pantelisstampoulis.androidtemplateproject.domain.onError
import com.pantelisstampoulis.androidtemplateproject.domain.onLoading
import com.pantelisstampoulis.androidtemplateproject.domain.onSuccess
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetWatchedMoviesUseCase
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.R
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

    init {
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
                                errorRes = null,
                                data = it.map { movie -> mapper.fromDomainToUi(movie) }
                                    .toImmutableList(),
                            )
                        }
                    }
                    .onError {
                        setState {
                            copy(
                                isLoading = false,
                                errorRes = R.string.error_generic,
                            )
                        }
                    }
            }
        }
    }

    override fun handleEvents(event: WatchedMovieListEvent) {
        when (event) {
            is WatchedMovieListEvent.ShowMovieDetails -> setEffect {
                WatchedMovieListSideEffect.NavigateToMovieDetails(event.movieId)
            }
        }
    }
}

data class WatchedMovieListUiState(
    val isLoading: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val data: ImmutableList<WatchedMovieUiModel>? = null,
) : UiState
