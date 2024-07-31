package com.pantelisstampoulis.androidtemplateproject.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.navigation.movieCatalogGraph
import com.pantelisstampoulis.androidtemplateproject.ui.screen.HomeScreen
import kotlinx.serialization.Serializable

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    startDestination: Any,
) {
    val mainNavController = rememberNavController()
    NavHost(
        modifier = modifier,
        navController = mainNavController,
        startDestination = startDestination,
    ) {

        composable<HomeScreenDestination> {
            HomeScreen(mainNavController)
        }

        // add feature Graphs here
        movieCatalogGraph(mainNavController)
    }
}


@Serializable
object HomeScreenDestination

