package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviesearch

import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.Event

sealed interface MovieSearchEvent : Event {

    /** The raw field text, sent on every change and again after rotation or process death. */
    data class QueryChanged(val text: String) : MovieSearchEvent

    data object Retry : MovieSearchEvent

    data object Refresh : MovieSearchEvent

    data class ShowMovieDetails(val movieId: Int) : MovieSearchEvent
}
