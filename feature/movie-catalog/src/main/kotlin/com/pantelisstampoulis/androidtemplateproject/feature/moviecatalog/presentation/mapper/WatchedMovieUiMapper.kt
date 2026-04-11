package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper

import com.pantelisstampoulis.androidtemplateproject.architecture.mapper.DomainToUiMapper
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.WatchedMovieUiModel
import com.pantelisstampoulis.androidtemplateproject.model.movies.WatchedMovie

class WatchedMovieUiMapper : DomainToUiMapper<WatchedMovie, WatchedMovieUiModel> {

    override fun fromDomainToUi(domainModel: WatchedMovie): WatchedMovieUiModel =
        WatchedMovieUiModel(
            movieId = domainModel.movieId,
            title = domainModel.title,
            posterPath = domainModel.posterUrl,
            voteAverage = domainModel.publicRating,
            userRating = domainModel.userRating,
            releaseYear = domainModel.releaseDate
                ?.split("-")
                ?.firstOrNull()
                .orEmpty(),
        )
}
