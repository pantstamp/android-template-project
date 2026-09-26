package com.pantelisstampoulis.androidtemplateproject.data.mapper.movie

import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.network.IMAGE_URL
import com.pantelisstampoulis.androidtemplateproject.test.doubles.network.NetworkTestDoubleFactory
import org.junit.Test

class MovieDataMapperTest {

    private val mapper = MovieDataMapper()

    @Test
    fun `fromApiToDb prefixes posterPath with the image url`() {
        val apiModel = NetworkTestDoubleFactory.provideMovieApiModel().copy(posterPath = "/poster.jpg")

        val dbModel = mapper.fromApiToDb(apiModel)

        assertThat(dbModel.posterPath).isEqualTo("$IMAGE_URL/poster.jpg")
    }

    @Test
    fun `fromApiToDb keeps null image paths null`() {
        val apiModel = NetworkTestDoubleFactory.provideMovieApiModel().copy(
            backdropPath = null,
            posterPath = null,
        )

        val dbModel = mapper.fromApiToDb(apiModel)

        assertThat(dbModel.backdropPath).isNull()
        assertThat(dbModel.posterPath).isNull()
    }

    @Test
    fun `fromApiToDb takes the first genre`() {
        val apiModel = NetworkTestDoubleFactory.provideMovieApiModel().copy(genreIds = listOf(878, 28, 12))

        val dbModel = mapper.fromApiToDb(apiModel)

        assertThat(dbModel.genreId).isEqualTo(878)
    }

    @Test
    fun `fromApiToDb maps empty genres to a null genreId`() {
        val apiModel = NetworkTestDoubleFactory.provideMovieApiModel().copy(genreIds = emptyList())

        val dbModel = mapper.fromApiToDb(apiModel)

        assertThat(dbModel.genreId).isNull()
    }

    @Test
    fun `fromApiToDb keeps a present release date`() {
        val apiModel = NetworkTestDoubleFactory.provideMovieApiModel().copy(releaseDate = "2026-07-29")

        val dbModel = mapper.fromApiToDb(apiModel)

        assertThat(dbModel.releaseDate).isEqualTo("2026-07-29")
    }

    @Test
    fun `fromApiToDb maps a blank release date to null`() {
        val apiModel = NetworkTestDoubleFactory.provideMovieApiModel().copy(releaseDate = "")

        val dbModel = mapper.fromApiToDb(apiModel)

        assertThat(dbModel.releaseDate).isNull()
    }
}
