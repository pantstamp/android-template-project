package com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.screen.movie_details


import com.pantelisstampoulis.androidtemplateproject.domain.onError
import com.pantelisstampoulis.androidtemplateproject.domain.onLoading
import com.pantelisstampoulis.androidtemplateproject.domain.onSuccess
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetMovieUseCase
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.ui_model.MovieUiModel
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.mapper.MovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.MviViewModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.UiState
import kotlinx.coroutines.launch

class MovieDetailsViewModel(
    private val getMovieUseCase: GetMovieUseCase,
    private val mapper: MovieUiMapper
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
                                        data = mapper.fromDomainToUi(it)
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
            }
        }
    }
}

data class MovieDetailsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val data: MovieUiModel? = null,
) : UiState
