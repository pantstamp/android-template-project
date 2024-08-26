package com.pantelisstampoulis.androidtemplateproject.database

import com.pantelisstampoulis.androidtemplateproject.database.model.MovieDbModel
import com.pantelisstampoulis.androidtemplateproject.database.model.MovieEntity
import kotlin.random.Random

object DatabaseTestDoubleFactory {

    fun provideMovieEntity() = MovieEntity(
        id = Random.nextInt(from = 1, until = 1000),
        adult = false,
        backdropPath = "",
        genreId = 12,
        originalLanguage = "",
        originalTitle = "",
        overview = "",
        popularity = 9.0,
        posterPath = "",
        releaseDate = "",
        title = "",
        video = false,
        voteAverage = 8.7,
        voteCount = 1000
    )

    fun provideMovieDbModel() = MovieDbModel(
        id = Random.nextInt(from = 1, until = 1000),
        adult = false,
        backdropPath = "",
        genreId = 12,
        originalLanguage = "",
        originalTitle = "",
        overview = "",
        popularity = 9.0,
        posterPath = "",
        releaseDate = "",
        title = "",
        video = false,
        voteAverage = 8.7,
        voteCount = 1000
    )

}