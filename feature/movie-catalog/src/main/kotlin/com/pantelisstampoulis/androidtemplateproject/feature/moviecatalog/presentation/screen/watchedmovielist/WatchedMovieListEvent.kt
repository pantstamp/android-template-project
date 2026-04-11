package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.watchedmovielist

import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.Event

sealed interface WatchedMovieListEvent : Event {
    data object GetWatchedMovies : WatchedMovieListEvent
    data class ShowMovieDetails(val movieId: Int) : WatchedMovieListEvent
}
