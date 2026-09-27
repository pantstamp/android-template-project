package com.pantelisstampoulis.androidtemplateproject.data.mapper.movie

import com.pantelisstampoulis.androidtemplateproject.architecture.mapper.ApiToDomainMapper
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import com.pantelisstampoulis.androidtemplateproject.network.model.MovieApiModel

/**
 * Maps a search result straight to the domain. Search results are never stored, so unlike
 * Discover they do not pass through a database model.
 */
internal class MovieSearchDomainMapper : ApiToDomainMapper<MovieApiModel, Movie> {

    override fun fromApiToDomain(apiModel: MovieApiModel): Movie = Movie(
        id = apiModel.id,
        adult = apiModel.adult,
        backdropPath = apiModel.backdropPath,
        genreId = apiModel.genreIds.firstOrNull(),
        originalLanguage = apiModel.originalLanguage,
        originalTitle = apiModel.originalTitle,
        overview = apiModel.overview,
        popularity = apiModel.popularity,
        posterPath = apiModel.posterPath.toPosterUrl(),
        releaseDate = apiModel.releaseDate.toReleaseDateOrNull(),
        title = apiModel.title,
        video = apiModel.video,
        voteAverage = apiModel.voteAverage,
        voteCount = apiModel.voteCount,
    )
}
