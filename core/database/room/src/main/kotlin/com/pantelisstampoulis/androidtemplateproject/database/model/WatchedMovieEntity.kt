package com.pantelisstampoulis.androidtemplateproject.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watched_movies")
data class WatchedMovieEntity(
    @PrimaryKey val movieId: Int,
    val title: String,
    val posterUrl: String?,
    val overview: String?,
    val publicRating: Double,
    val releaseDate: String?,
    val userRating: Int,
    val ratedAt: Long,
)
