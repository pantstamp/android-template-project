package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper

import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.R
import com.pantelisstampoulis.androidtemplateproject.test.doubles.model.DomainTestDoubleFactory
import org.junit.Test

class MovieUiMapperTest {

    private val mapper = MovieUiMapper()

    @Test
    fun `fromDomainToUi extracts the year from the release date`() {
        val movie = DomainTestDoubleFactory.provideMovieModel().copy(releaseDate = "2026-07-29")

        val uiModel = mapper.fromDomainToUi(movie)

        assertThat(uiModel.releaseYear).isEqualTo("2026")
    }

    @Test
    fun `fromDomainToUi maps a missing release date to an empty year`() {
        val movie = DomainTestDoubleFactory.provideMovieModel().copy(releaseDate = null)

        val uiModel = mapper.fromDomainToUi(movie)

        assertThat(uiModel.releaseYear).isEmpty()
    }

    @Test
    fun `fromDomainToUi maps a missing genre to the unknown genre`() {
        val movie = DomainTestDoubleFactory.provideMovieModel().copy(genreId = null)

        val uiModel = mapper.fromDomainToUi(movie)

        assertThat(uiModel.genreStringId).isEqualTo(R.string.genre_unknown)
    }
}
