package com.pantelisstampoulis.androidtemplateproject.data.mapper.movie

import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import com.pantelisstampoulis.androidtemplateproject.network.IMAGE_URL
import com.pantelisstampoulis.androidtemplateproject.test.doubles.network.NetworkTestDoubleFactory
import org.junit.Test

class MovieSearchDomainMapperTest {

    private val mapper = MovieSearchDomainMapper()

    @Test
    fun `fromApiToDomain maps every field`() {
        val apiModel = NetworkTestDoubleFactory.provideMovieApiModel().copy(
            genreIds = listOf(878, 28),
            posterPath = "/poster.jpg",
            releaseDate = "1999-03-31",
        )

        val movie = mapper.fromApiToDomain(apiModel)

        assertThat(movie).isEqualTo(
            Movie(
                id = apiModel.id,
                adult = apiModel.adult,
                backdropPath = apiModel.backdropPath,
                genreId = 878,
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
    fun `fromApiToDomain keeps a null poster null`() {
        val apiModel = NetworkTestDoubleFactory.provideMovieApiModel().copy(posterPath = null)

        assertThat(mapper.fromApiToDomain(apiModel).posterPath).isNull()
    }

    @Test
    fun `fromApiToDomain maps a blank release date to null`() {
        val apiModel = NetworkTestDoubleFactory.provideMovieApiModel().copy(releaseDate = "")

        assertThat(mapper.fromApiToDomain(apiModel).releaseDate).isNull()
    }

    @Test
    fun `fromApiToDomain maps empty genres to a null genreId`() {
        val apiModel = NetworkTestDoubleFactory.provideMovieApiModel().copy(genreIds = emptyList())

        assertThat(mapper.fromApiToDomain(apiModel).genreId).isNull()
    }
}
