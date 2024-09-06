package com.pantelisstampoulis.androidtemplateproject.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.navigation.movieCatalogGraph
import com.pantelisstampoulis.androidtemplateproject.ui.screen.HomeScreen
import org.koin.compose.getKoin
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    startDestination: Any,
) {
    // Injecting the NavController into Koin
    val mainNavController = rememberNavController()
    getKoin().setProperty(NavigationConstants.NAVIGATION_CONTROLLER, mainNavController)

    // inject navigator
    val navigator: Navigator = koinInject()

    NavHost(
        modifier = modifier,
        navController = mainNavController,
        startDestination = startDestination,
    ) {
        composable<AppDestination.HomeScreenDestination> {
            HomeScreen(navigator)
        }

        // add feature Graphs here
        movieCatalogGraph(navigator)
    }
}
