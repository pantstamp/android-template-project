package com.pantelisstampoulis.androidtemplateproject.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "movies",
)
data class MovieEntity(
    @PrimaryKey val id: Int,
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
    val voteCount: Int,
)
