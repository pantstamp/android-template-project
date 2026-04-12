package com.pantelisstampoulis.androidtemplateproject.model.movies

data class WatchedMovie(
    val movieId: Int,
    val title: String,
    val posterUrl: String?,
    val overview: String?,
    val publicRating: Double,
    val releaseDate: String?,
    val userRating: Int,
    val ratedAt: Long,
)
