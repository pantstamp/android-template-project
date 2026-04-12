package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.watchedmovielist

import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.SideEffect

sealed interface WatchedMovieListSideEffect : SideEffect {
    data class NavigateToMovieDetails(val movieId: Int) : WatchedMovieListSideEffect
}
