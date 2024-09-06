package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviedetails

import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.Event

sealed interface MovieDetailsEvent : Event {

    data class Init(val movieId: Int) : MovieDetailsEvent

    data class RateMovie(val movieId: Int, val rating: Float) : MovieDetailsEvent
}
