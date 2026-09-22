package com.pantelisstampoulis.androidtemplateproject.database.mapper

import com.pantelisstampoulis.androidtemplateproject.architecture.mapper.DbToEntityMapper
import com.pantelisstampoulis.androidtemplateproject.architecture.mapper.EntityToDbMapper
import com.pantelisstampoulis.androidtemplateproject.database.model.WatchedMovieDbModel
import com.pantelisstampoulis.androidtemplateproject.database.model.WatchedMovieEntity

internal class WatchedMovieDbMapper :
    DbToEntityMapper<WatchedMovieDbModel, WatchedMovieEntity>,
    EntityToDbMapper<WatchedMovieEntity, WatchedMovieDbModel> {

    override fun fromDbToEntity(dbModel: WatchedMovieDbModel): WatchedMovieEntity = WatchedMovieEntity(
        movieId = dbModel.movieId,
        title = dbModel.title,
        posterUrl = dbModel.posterUrl,
        overview = dbModel.overview,
        publicRating = dbModel.publicRating,
        releaseDate = dbModel.releaseDate,
        userRating = dbModel.userRating,
        ratedAt = dbModel.ratedAt,
    )

    override fun fromEntityToDb(entity: WatchedMovieEntity): WatchedMovieDbModel = WatchedMovieDbModel(
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
