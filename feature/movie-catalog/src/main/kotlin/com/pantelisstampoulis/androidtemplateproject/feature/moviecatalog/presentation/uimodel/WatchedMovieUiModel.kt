package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel

import androidx.compose.runtime.Immutable

@Immutable
data class WatchedMovieUiModel(
    val movieId: Int,
    val title: String,
    val posterPath: String?,
    val voteAverage: Double,
    val userRating: Int,
    val releaseYear: String,
)
