package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist

import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.Event

sealed interface MovieListEvent : Event {

    data object Refresh : MovieListEvent
    data class ShowMovieDetails(val movieId: Int) : MovieListEvent
}
