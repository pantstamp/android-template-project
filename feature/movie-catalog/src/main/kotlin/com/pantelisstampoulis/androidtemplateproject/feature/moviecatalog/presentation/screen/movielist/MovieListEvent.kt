package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist

import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.Event

sealed interface MovieListEvent : Event {

    data class GetMovies(val ignoreCache: Boolean = false) : MovieListEvent
    data class ShowMovieDetails(val movieId: Int) : MovieListEvent
}
