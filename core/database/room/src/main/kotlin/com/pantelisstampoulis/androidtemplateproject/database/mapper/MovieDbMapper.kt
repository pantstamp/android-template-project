package com.pantelisstampoulis.androidtemplateproject.database.mapper

import com.pantelisstampoulis.androidtemplateproject.database.model.MovieDbModel
import com.pantelisstampoulis.androidtemplateproject.database.model.MovieEntity

class MovieDbMapper {

    internal fun toDb(
        model: MovieDbModel,
    ): MovieEntity = MovieEntity(
        id = model.id,
        adult = model.adult,
        backdropPath = model.backdropPath,
        genreId = model.genreId,
        originalLanguage = model.originalLanguage,
        originalTitle = model.originalTitle,
        overview = model.overview,
        popularity = model.popularity,
        posterPath = model.posterPath,
        releaseDate = model.releaseDate,
        title = model.title,
        video = model.video,
        voteAverage = model.voteAverage,
        voteCount = model.voteCount,
    )

    internal fun mapFromDb(
        model: MovieEntity,
    ): MovieDbModel = MovieDbModel(
        id = model.id,
        adult = model.adult,
        backdropPath = model.backdropPath,
        genreId = model.genreId,
        originalLanguage = model.originalLanguage,
        originalTitle = model.originalTitle,
        overview = model.overview,
        popularity = model.popularity,
        posterPath = model.posterPath,
        releaseDate = model.releaseDate,
        title = model.title,
        video = model.video,
        voteAverage = model.voteAverage,
        voteCount = model.voteCount,
    )
}
