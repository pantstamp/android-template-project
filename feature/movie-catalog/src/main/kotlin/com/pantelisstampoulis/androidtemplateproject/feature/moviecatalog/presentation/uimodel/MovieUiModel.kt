package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel

import androidx.compose.runtime.Immutable

@Immutable
data class MovieUiModel(
    val id: Int,
    val adult: Boolean,
    val backdropPath: String,
    val genreStringId: Int,
    val originalLanguage: String,
    val originalTitle: String,
    val overview: String,
    val popularity: Double,
    val posterPath: String,
    val releaseYear: String,
    val title: String,
    val video: Boolean,
    val voteAverage: Double,
    val voteCount: Int,
)
