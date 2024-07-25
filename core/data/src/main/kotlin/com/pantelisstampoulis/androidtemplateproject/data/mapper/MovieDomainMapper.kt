package com.pantelisstampoulis.androidtemplateproject.data.mapper

import com.pantelisstampoulis.androidtemplateproject.database.model.MovieDbModel
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import com.sregs.architecture.mapper.DbToDomainMapper

internal class MovieDomainMapper:
    DbToDomainMapper<MovieDbModel, Movie> {

    override fun fromDbToDomain(
        dbModel: MovieDbModel,
    ): Movie = Movie(
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
        voteCount = dbModel.voteCount
    )
}
