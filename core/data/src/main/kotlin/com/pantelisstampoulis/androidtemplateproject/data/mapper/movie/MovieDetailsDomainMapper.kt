package com.pantelisstampoulis.androidtemplateproject.data.mapper.movie

import com.pantelisstampoulis.androidtemplateproject.architecture.mapper.ApiToDomainMapper
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import com.pantelisstampoulis.androidtemplateproject.network.model.MovieDetailsApiModel

/** Maps a movie fetched by id, which is shown in Details but never stored. */
internal class MovieDetailsDomainMapper : ApiToDomainMapper<MovieDetailsApiModel, Movie> {

    override fun fromApiToDomain(apiModel: MovieDetailsApiModel): Movie = Movie(
        id = apiModel.id,
        adult = apiModel.adult,
        backdropPath = apiModel.backdropPath,
        genreId = apiModel.genres.firstOrNull()?.id,
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
