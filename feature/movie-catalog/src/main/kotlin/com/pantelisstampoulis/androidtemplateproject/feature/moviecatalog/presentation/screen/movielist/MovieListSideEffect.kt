package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist

import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.error.LoadError
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.SideEffect

sealed interface MovieListSideEffect : SideEffect {

    data class RefreshFailed(val error: LoadError) : MovieListSideEffect

    data class NavigateToMovieDetails(val movieId: Int) : MovieListSideEffect
}
