package com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.screen.movie_list

import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.Event


sealed interface MovieListEvent : Event {

    data object Init : MovieListEvent
    data class ShowMovieDetails(val movieId: Int) : MovieListEvent
}
