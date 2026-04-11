package com.pantelisstampoulis.androidtemplateproject.data.mapper

import com.pantelisstampoulis.androidtemplateproject.architecture.mapper.DbToDomainMapper
import com.pantelisstampoulis.androidtemplateproject.database.model.WatchedMovieDbModel
import com.pantelisstampoulis.androidtemplateproject.model.movies.WatchedMovie

internal class WatchedMovieDomainMapper : DbToDomainMapper<WatchedMovieDbModel, WatchedMovie> {
    override fun fromDbToDomain(dbModel: WatchedMovieDbModel): WatchedMovie = WatchedMovie(
        movieId = dbModel.movieId,
        title = dbModel.title,
        posterUrl = dbModel.posterUrl,
        overview = dbModel.overview,
        publicRating = dbModel.publicRating,
        releaseDate = dbModel.releaseDate,
        userRating = dbModel.userRating,
        ratedAt = dbModel.ratedAt,
    )
}
