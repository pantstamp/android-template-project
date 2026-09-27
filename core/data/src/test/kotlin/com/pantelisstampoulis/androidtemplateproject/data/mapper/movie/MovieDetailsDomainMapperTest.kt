package com.pantelisstampoulis.androidtemplateproject.data.mapper.movie

import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import com.pantelisstampoulis.androidtemplateproject.network.IMAGE_URL
import com.pantelisstampoulis.androidtemplateproject.network.model.GenreApiModel
import com.pantelisstampoulis.androidtemplateproject.test.doubles.network.NetworkTestDoubleFactory
import org.junit.Test

class MovieDetailsDomainMapperTest {

    private val mapper = MovieDetailsDomainMapper()

    @Test
    fun `fromApiToDomain maps every field`() {
        val apiModel = NetworkTestDoubleFactory.provideMovieDetailsApiModel().copy(
            genres = listOf(GenreApiModel(id = 28, name = "Action"), GenreApiModel(id = 878, name = "Sci-Fi")),
            posterPath = "/poster.jpg",
            releaseDate = "1999-03-31",
        )

        val movie = mapper.fromApiToDomain(apiModel)

        assertThat(movie).isEqualTo(
            Movie(
                id = apiModel.id,
                adult = apiModel.adult,
                backdropPath = apiModel.backdropPath,
                genreId = 28,
                originalLanguage = apiModel.originalLanguage,
                originalTitle = apiModel.originalTitle,
                overview = apiModel.overview,
                popularity = apiModel.popularity,
                posterPath = "$IMAGE_URL/poster.jpg",
                releaseDate = "1999-03-31",
                title = apiModel.title,
                video = apiModel.video,
                voteAverage = apiModel.voteAverage,
                voteCount = apiModel.voteCount,
            ),
        )
    }

    @Test
    fun `fromApiToDomain maps empty genres to a null genreId`() {
        val apiModel = NetworkTestDoubleFactory.provideMovieDetailsApiModel().copy(genres = emptyList())

        assertThat(mapper.fromApiToDomain(apiModel).genreId).isNull()
    }

    @Test
    fun `fromApiToDomain keeps a null poster null`() {
        val apiModel = NetworkTestDoubleFactory.provideMovieDetailsApiModel().copy(posterPath = null)

        assertThat(mapper.fromApiToDomain(apiModel).posterPath).isNull()
    }

    @Test
    fun `fromApiToDomain maps a blank release date to null`() {
        val apiModel = NetworkTestDoubleFactory.provideMovieDetailsApiModel().copy(releaseDate = "")

        assertThat(mapper.fromApiToDomain(apiModel).releaseDate).isNull()
    }
}
