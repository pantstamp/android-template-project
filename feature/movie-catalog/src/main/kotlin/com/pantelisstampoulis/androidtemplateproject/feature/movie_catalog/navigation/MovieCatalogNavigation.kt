package com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.screen.movie_list.MovieListScreen
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.screen.movie_list.MovieListViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object MovieCatalogDestination

@Serializable
object MovieListDestination

fun NavGraphBuilder.movieCatalogGraph(
    navController: NavHostController,
) {
    navigation<MovieCatalogDestination>(startDestination = MovieListDestination) {
        composable<MovieListDestination> {
            val viewModel = koinViewModel<MovieListViewModel>()
            val state by viewModel.viewState.collectAsStateWithLifecycle()
            MovieListScreen(
                state = state,
                effect = viewModel.effect,
                onEvent = viewModel::setEvent
            )
        }

    }
}