package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviedetails

import com.pantelisstampoulis.androidtemplateproject.domain.onError
import com.pantelisstampoulis.androidtemplateproject.domain.onLoading
import com.pantelisstampoulis.androidtemplateproject.domain.onSuccess
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetMovieUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.RateMovieUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.RateMovieUseCaseInput
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper.MovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.MovieUiModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.MviViewModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.UiState
import kotlinx.coroutines.launch

class MovieDetailsViewModel(
    private val getMovieUseCase: GetMovieUseCase,
    private val rateMovieUseCase: RateMovieUseCase,
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
            }

            is MovieDetailsEvent.RateMovie -> {
                viewModelScope.launch {
                    rateMovieUseCase.invoke(
                        RateMovieUseCaseInput(
                            event.movieId,
                            event.rating,
                        ),
                    ).collect { resultState ->
                        resultState
                            .onSuccess {
                                setEffect {
                                    MovieDetailsSideEffect.ShowToast("Movie rated successfully")
                                }
                            }
                            .onError { error ->
                                setEffect {
                                    MovieDetailsSideEffect.ShowToast("Error while rating Movie: ${error.message}")
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
