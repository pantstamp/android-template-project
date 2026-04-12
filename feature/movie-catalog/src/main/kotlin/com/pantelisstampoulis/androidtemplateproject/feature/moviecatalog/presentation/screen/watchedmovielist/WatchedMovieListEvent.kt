package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.watchedmovielist

import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.Event

sealed interface WatchedMovieListEvent : Event {
    data class ShowMovieDetails(val movieId: Int) : WatchedMovieListEvent
}
