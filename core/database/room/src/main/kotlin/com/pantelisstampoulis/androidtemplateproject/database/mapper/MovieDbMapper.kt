package com.pantelisstampoulis.androidtemplateproject.database.mapper

import com.pantelisstampoulis.androidtemplateproject.architecture.mapper.DbToEntityMapper
import com.pantelisstampoulis.androidtemplateproject.architecture.mapper.EntityToDbMapper
import com.pantelisstampoulis.androidtemplateproject.database.model.MovieDbModel
import com.pantelisstampoulis.androidtemplateproject.database.model.MovieEntity

internal class MovieDbMapper :
    DbToEntityMapper<MovieDbModel, MovieEntity>,
    EntityToDbMapper<MovieEntity, MovieDbModel> {

    override fun fromDbToEntity(dbModel: MovieDbModel): MovieEntity = MovieEntity(
        id = dbModel.id,
        adult = dbModel.adult,
        backdropPath = dbModel.backdropPath,
        genreId = dbModel.genreId,
        originalLanguage = dbModel.originalLanguage,
        originalTitle = dbModel.originalTitle,
        overview = dbModel.overview,
        popularity = dbModel.popularity,
        posterPath = dbModel.posterPath,
        releaseDate = dbModel.releaseDate,
        title = dbModel.title,
        video = dbModel.video,
        voteAverage = dbModel.voteAverage,
        voteCount = dbModel.voteCount,
    )

    override fun fromEntityToDb(entity: MovieEntity): MovieDbModel = MovieDbModel(
        id = entity.id,
        adult = entity.adult,
        backdropPath = entity.backdropPath,
        genreId = entity.genreId,
        originalLanguage = entity.originalLanguage,
        originalTitle = entity.originalTitle,
        overview = entity.overview,
        popularity = entity.popularity,
        posterPath = entity.posterPath,
        releaseDate = entity.releaseDate,
        title = entity.title,
        video = entity.video,
        voteAverage = entity.voteAverage,
        voteCount = entity.voteCount,
    )
}
