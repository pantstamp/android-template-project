package com.pantelisstampoulis.androidtemplateproject.database.model

data class WatchedMovieDbModel(
    val movieId: Int,
    val title: String,
    val posterUrl: String?,
    val overview: String?,
    val publicRating: Double,
    val releaseDate: String?,
    val userRating: Int,
    val ratedAt: Long,
)
