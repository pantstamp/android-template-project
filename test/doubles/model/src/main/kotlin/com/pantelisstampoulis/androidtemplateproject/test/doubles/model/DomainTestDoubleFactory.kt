package com.pantelisstampoulis.androidtemplateproject.test.doubles.model

import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import com.pantelisstampoulis.androidtemplateproject.model.movies.WatchedMovie
import com.pantelisstampoulis.androidtemplateproject.random.randomBoolean
import com.pantelisstampoulis.androidtemplateproject.random.randomFloat
import com.pantelisstampoulis.androidtemplateproject.random.randomInt
import com.pantelisstampoulis.androidtemplateproject.random.randomString

object DomainTestDoubleFactory {

    fun provideMovieModel() = Movie(
        id = randomInt(from = 1, until = 1000),
        adult = randomBoolean(),
        backdropPath = randomString(),
        genreId = randomInt(from = 1, until = 100),
        originalLanguage = randomString(),
        originalTitle = randomString(),
        overview = randomString(),
        popularity = randomFloat().toDouble(),
        posterPath = randomString(),
        releaseDate = randomString(),
        title = randomString(),
        video = randomBoolean(),
        voteAverage = randomFloat(from = 0F, until = 10F).toDouble(),
        voteCount = randomInt(from = 0),
    )

    fun provideWatchedMovieModel() = WatchedMovie(
        movieId = randomInt(from = 1, until = 1000),
        title = randomString(),
        posterUrl = randomString(),
        overview = randomString(),
        publicRating = randomFloat(from = 0F, until = 10F).toDouble(),
        releaseDate = randomString(),
        userRating = randomInt(from = 1, until = 10),
        ratedAt = System.currentTimeMillis(),
    )
}
