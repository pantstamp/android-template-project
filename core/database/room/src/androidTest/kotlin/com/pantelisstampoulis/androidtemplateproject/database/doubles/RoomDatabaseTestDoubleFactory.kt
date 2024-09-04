package com.pantelisstampoulis.androidtemplateproject.database.doubles

import com.pantelisstampoulis.androidtemplateproject.database.model.MovieEntity
import com.pantelisstampoulis.androidtemplateproject.random.randomBoolean
import com.pantelisstampoulis.androidtemplateproject.random.randomFloat
import com.pantelisstampoulis.androidtemplateproject.random.randomInt
import com.pantelisstampoulis.androidtemplateproject.random.randomString

object RoomDatabaseTestDoubleFactory {

    fun provideMovieEntity() = MovieEntity(
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
        voteCount = randomInt(from = 0)
    )
}
