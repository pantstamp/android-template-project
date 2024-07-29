package com.pantelisstampoulis.androidtemplateproject.data.mapper

import com.pantelisstampoulis.androidtemplateproject.database.model.MovieDbModel
import com.pantelisstampoulis.androidtemplateproject.network.model.MovieApiModel
import com.pantelisstampoulis.androidtemplateproject.architecture.mapper.ApiToDbMapper

internal class MovieDataMapper: ApiToDbMapper<MovieApiModel, MovieDbModel> {

    override fun fromApiToDb(
        apiModel: MovieApiModel,
    ): MovieDbModel = MovieDbModel(
        id = apiModel.id,
        adult = apiModel.adult,
        backdropPath = apiModel.backdropPath,
        genreId = apiModel.genreIds[0],
        originalLanguage = apiModel.originalLanguage,
        originalTitle = apiModel.originalTitle,
        overview = apiModel.overview,
        popularity = apiModel.popularity,
        posterPath = apiModel.posterPath,
        releaseDate = apiModel.releaseDate,
        title = apiModel.title,
        video = apiModel.video,
        voteAverage = apiModel.voteAverage,
        voteCount = apiModel.voteCount
    )
}
