package com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.presentation.screen.movie_list

import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.Event


sealed interface MovieListEvent : Event {

    data class GetMovies(val ignoreCache: Boolean = false) : MovieListEvent
    data class ShowMovieDetails(val movieId: Int) : MovieListEvent
}
