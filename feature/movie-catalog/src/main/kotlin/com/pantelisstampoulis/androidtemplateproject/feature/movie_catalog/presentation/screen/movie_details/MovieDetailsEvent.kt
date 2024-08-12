package com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.presentation.screen.movie_details

import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.Event


sealed interface MovieDetailsEvent : Event {

    data class Init(val movieId: Int) : MovieDetailsEvent

    data class RateMovie(val movieId: Int, val rating: Float) : MovieDetailsEvent
}
