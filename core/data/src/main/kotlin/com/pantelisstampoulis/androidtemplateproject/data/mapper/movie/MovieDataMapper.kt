package com.pantelisstampoulis.androidtemplateproject.data.mapper.movie

import com.pantelisstampoulis.androidtemplateproject.architecture.mapper.ApiToDbMapper
import com.pantelisstampoulis.androidtemplateproject.database.model.MovieDbModel
import com.pantelisstampoulis.androidtemplateproject.network.IMAGE_URL
import com.pantelisstampoulis.androidtemplateproject.network.model.MovieApiModel

internal class MovieDataMapper : ApiToDbMapper<MovieApiModel, MovieDbModel> {

    override fun fromApiToDb(apiModel: MovieApiModel): MovieDbModel = MovieDbModel(
        id = apiModel.id,
        adult = apiModel.adult,
        backdropPath = apiModel.backdropPath,
        genreId = apiModel.genreIds.firstOrNull(),
        originalLanguage = apiModel.originalLanguage,
        originalTitle = apiModel.originalTitle,
        overview = apiModel.overview,
        popularity = apiModel.popularity,
        posterPath = apiModel.posterPath?.let { IMAGE_URL + it },
        releaseDate = apiModel.releaseDate?.takeIf { it.isNotBlank() },
        title = apiModel.title,
        video = apiModel.video,
        voteAverage = apiModel.voteAverage,
        voteCount = apiModel.voteCount,
    )
}
