package com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.movie_list

data class MovieUiModel(
    val id: Int,
    val adult: Boolean,
    val backdropPath: String,
    val genreId: Int,
    val originalLanguage: String,
    val originalTitle: String,
    val overview: String,
    val popularity: Double,
    val posterPath: String,
    val releaseDate: String,
    val title: String,
    val video: Boolean,
    val voteAverage: Double,
    val voteCount: Int
)
