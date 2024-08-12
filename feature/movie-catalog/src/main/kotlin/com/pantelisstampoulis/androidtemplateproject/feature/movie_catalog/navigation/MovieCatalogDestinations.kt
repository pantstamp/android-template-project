package com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.navigation

import kotlinx.serialization.Serializable

sealed interface MovieCatalogDestination {

    @Serializable
    data object MovieCatalogHomeDestination: MovieCatalogDestination

    @Serializable
    data object MovieListDestination: MovieCatalogDestination

    @Serializable
    data class MovieDetailsDestination(val movieId: Int): MovieCatalogDestination
}