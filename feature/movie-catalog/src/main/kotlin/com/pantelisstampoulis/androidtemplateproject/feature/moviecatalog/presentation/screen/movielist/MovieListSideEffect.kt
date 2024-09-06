package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist

import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.SideEffect

sealed interface MovieListSideEffect : SideEffect {

    data class ShowToast(val text: String) : MovieListSideEffect

    data class NavigateToMovieDetails(val movieId: Int) : MovieListSideEffect
}
