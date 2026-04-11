package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.R
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist.MovieListEvent
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist.MovieListScreen
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist.MovieListSideEffect
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist.MovieListUiState
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.watchedmovielist.WatchedMovieListEvent
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.watchedmovielist.WatchedMovieListScreen
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.watchedmovielist.WatchedMovieListSideEffect
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.watchedmovielist.WatchedMovieListUiState
import kotlinx.coroutines.flow.Flow

@Composable
fun MovieCatalogTabbedScreen(
    movieListState: MovieListUiState,
    movieListEffect: Flow<MovieListSideEffect>,
    onMovieListEvent: (MovieListEvent) -> Unit,
    watchedMovieListState: WatchedMovieListUiState,
    watchedMovieListEffect: Flow<WatchedMovieListSideEffect>,
    onWatchedMovieListEvent: (WatchedMovieListEvent) -> Unit,
    onMovieClicked: (Int) -> Unit,
) {
    val tabTitles = listOf(
        stringResource(R.string.tab_discover),
        stringResource(R.string.tab_watched),
    )
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) },
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedTabIndex == 0) {
                MovieListScreen(
                    state = movieListState,
                    effect = movieListEffect,
                    onEvent = onMovieListEvent,
                    onMovieClicked = onMovieClicked,
                )
            }
            if (selectedTabIndex == 1) {
                WatchedMovieListScreen(
                    state = watchedMovieListState,
                    effect = watchedMovieListEffect,
                    onEvent = onWatchedMovieListEvent,
                    onMovieClicked = onMovieClicked,
                )
            }
        }
    }
}
