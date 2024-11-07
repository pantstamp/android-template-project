package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviedetails.MovieDetailsScreen
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviedetails.MovieDetailsViewModel
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist.MovieListScreen
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist.MovieListViewModel
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.movieCatalogGraph(
    onMovieClicked: (Int) -> Unit,
) {
    navigation<MovieCatalogDestination.MovieCatalogHomeDestination>(
        startDestination = MovieCatalogDestination.MovieListDestination,
    ) {
        addMovieListScreen(onMovieClicked)
        addMovieDetailsScreen()
    }
}

private fun NavGraphBuilder.addMovieListScreen(
    onMovieClicked: (Int) -> Unit,
) {
    composable<MovieCatalogDestination.MovieListDestination> {
        val viewModel = koinViewModel<MovieListViewModel>()
        val state by viewModel.viewState.collectAsStateWithLifecycle()
        MovieListScreen(
            state = state,
            effect = viewModel.effect,
            onEvent = viewModel::setEvent,
            onMovieClicked = onMovieClicked,
        )
    }
}

private fun NavGraphBuilder.addMovieDetailsScreen() {
    composable<MovieCatalogDestination.MovieDetailsDestination> {
        val args = it.toRoute<MovieCatalogDestination.MovieDetailsDestination>()
        val viewModel = koinViewModel<MovieDetailsViewModel>()
        val state by viewModel.viewState.collectAsStateWithLifecycle()
        MovieDetailsScreen(
            state = state,
            effect = viewModel.effect,
            onEvent = viewModel::setEvent,
            movieId = args.movieId,
        )
    }
}
