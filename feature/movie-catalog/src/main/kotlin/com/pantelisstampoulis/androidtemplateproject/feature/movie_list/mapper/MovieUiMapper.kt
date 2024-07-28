package com.pantelisstampoulis.androidtemplateproject.feature.movie_list.mapper

import com.pantelisstampoulis.androidtemplateproject.feature.movie_list.MovieUiModel
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import com.sregs.architecture.mapper.DomainToUiMapper

class MovieUiMapper: DomainToUiMapper<Movie, MovieUiModel> {

    override fun fromDomainToUi(
        domainModel: Movie,
    ): MovieUiModel = MovieUiModel(
        id = domainModel.id,
        adult = domainModel.adult,
        backdropPath = domainModel.backdropPath,
        genreId = domainModel.genreId,
        originalLanguage = domainModel.originalLanguage,
        originalTitle = domainModel.originalTitle,
        overview = domainModel.overview,
        popularity = domainModel.popularity,
        posterPath = domainModel.posterPath,
        releaseDate = domainModel.releaseDate,
        title = domainModel.title,
        video = domainModel.video,
        voteAverage = domainModel.voteAverage,
        voteCount = domainModel.voteCount,
    )
}
