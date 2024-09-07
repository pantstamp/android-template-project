package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper

import com.pantelisstampoulis.androidtemplateproject.architecture.mapper.DomainToUiMapper
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.R
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.MovieUiModel
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie

class MovieUiMapper : DomainToUiMapper<Movie, MovieUiModel> {

    override fun fromDomainToUi(
        domainModel: Movie,
    ): MovieUiModel = MovieUiModel(
        id = domainModel.id,
        adult = domainModel.adult,
        backdropPath = domainModel.backdropPath,
        genreStringId = getGenreStringResource(domainModel.genreId),
        originalLanguage = domainModel.originalLanguage,
        originalTitle = domainModel.originalTitle,
        overview = domainModel.overview,
        popularity = domainModel.popularity,
        posterPath = domainModel.posterPath,
        releaseYear = extractYearFromDate(domainModel.releaseDate),
        title = domainModel.title,
        video = domainModel.video,
        voteAverage = domainModel.voteAverage,
        voteCount = domainModel.voteCount,
    )

    private fun getGenreStringResource(genreId: Int): Int = when (genreId) {
        28 -> R.string.genre_action
        12 -> R.string.genre_adventure
        16 -> R.string.genre_animation
        35 -> R.string.genre_comedy
        80 -> R.string.genre_crime
        99 -> R.string.genre_documentary
        18 -> R.string.genre_drama
        10751 -> R.string.genre_family
        14 -> R.string.genre_fantasy
        36 -> R.string.genre_history
        27 -> R.string.genre_horror
        10402 -> R.string.genre_music
        9648 -> R.string.genre_mystery
        10749 -> R.string.genre_romance
        878 -> R.string.genre_science_fiction
        10770 -> R.string.genre_tv_movie
        53 -> R.string.genre_thriller
        10752 -> R.string.genre_war
        37 -> R.string.genre_western
        else -> R.string.genre_unknown // Default case if genre ID doesn't match
    }

    private fun extractYearFromDate(dateString: String): String = dateString.substring(0, 4)
}
