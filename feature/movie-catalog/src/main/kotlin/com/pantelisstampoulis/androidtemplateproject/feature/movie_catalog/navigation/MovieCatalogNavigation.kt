package com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.presentation.screen.movie_details.MovieDetailsScreen
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.presentation.screen.movie_details.MovieDetailsViewModel
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.presentation.screen.movie_list.MovieListScreen
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.presentation.screen.movie_list.MovieListViewModel
import com.pantelisstampoulis.androidtemplateproject.navigation.Navigator
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.movieCatalogGraph(
    navigator: Navigator,
) {
    navigation<MovieCatalogDestination.MovieCatalogHomeDestination>(
        startDestination = MovieCatalogDestination.MovieListDestination
    ) {
        addMovieListScreen(navigator)
        addMovieDetailsScreen(navigator)
    }
}

private fun NavGraphBuilder.addMovieListScreen(
    navigator: Navigator,
) {
    composable<MovieCatalogDestination.MovieListDestination> {
        val viewModel = koinViewModel<MovieListViewModel>()
        val state by viewModel.viewState.collectAsStateWithLifecycle()
        MovieListScreen(
            state = state,
            effect = viewModel.effect,
            onEvent = viewModel::setEvent,
            navigator = navigator
        )
    }
}

private fun NavGraphBuilder.addMovieDetailsScreen(
    navigator: Navigator,
) {
    composable<MovieCatalogDestination.MovieDetailsDestination> {
        val args = it.toRoute<MovieCatalogDestination.MovieDetailsDestination>()
        val viewModel = koinViewModel<MovieDetailsViewModel>()
        val state by viewModel.viewState.collectAsStateWithLifecycle()
        MovieDetailsScreen(
            state = state,
            effect = viewModel.effect,
            onEvent = viewModel::setEvent,
            movieId = args.movieId
        )
    }
}