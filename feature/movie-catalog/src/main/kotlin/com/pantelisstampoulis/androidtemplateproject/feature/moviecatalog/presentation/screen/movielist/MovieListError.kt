package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist

import com.pantelisstampoulis.androidtemplateproject.model.error.DomainError

enum class MovieListError { Offline, Generic }

internal fun DomainError.toMovieListError(): MovieListError = when (this) {
    is DomainError.NoNetworkConnection -> MovieListError.Offline
    else -> MovieListError.Generic
}
