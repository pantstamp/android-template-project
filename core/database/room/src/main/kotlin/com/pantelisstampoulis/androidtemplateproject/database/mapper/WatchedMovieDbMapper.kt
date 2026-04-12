package com.pantelisstampoulis.androidtemplateproject.database.mapper

import com.pantelisstampoulis.androidtemplateproject.database.model.WatchedMovieDbModel
import com.pantelisstampoulis.androidtemplateproject.database.model.WatchedMovieEntity

class WatchedMovieDbMapper {

    fun toDb(model: WatchedMovieDbModel): WatchedMovieEntity = WatchedMovieEntity(
        movieId = model.movieId,
        title = model.title,
        posterUrl = model.posterUrl,
        overview = model.overview,
        publicRating = model.publicRating,
        releaseDate = model.releaseDate,
        userRating = model.userRating,
        ratedAt = model.ratedAt,
    )

    fun mapFromDb(entity: WatchedMovieEntity): WatchedMovieDbModel = WatchedMovieDbModel(
        movieId = entity.movieId,
        title = entity.title,
        posterUrl = entity.posterUrl,
        overview = entity.overview,
        publicRating = entity.publicRating,
        releaseDate = entity.releaseDate,
        userRating = entity.userRating,
        ratedAt = entity.ratedAt,
    )
}
