package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
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
import kotlinx.coroutines.flow.emptyFlow

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
    val pagerState = rememberPagerState { tabTitles.size }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title) },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> MovieListScreen(
                    state = movieListState,
                    effect = movieListEffect,
                    onEvent = onMovieListEvent,
                    onMovieClicked = onMovieClicked,
                )
                1 -> WatchedMovieListScreen(
                    state = watchedMovieListState,
                    effect = watchedMovieListEffect,
                    onEvent = onWatchedMovieListEvent,
                    onMovieClicked = onMovieClicked,
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewMovieCatalogTabbedScreen() {
    MovieCatalogTabbedScreen(
        movieListState = MovieListUiState(isLoading = true),
        movieListEffect = emptyFlow(),
        onMovieListEvent = {},
        watchedMovieListState = WatchedMovieListUiState(),
        watchedMovieListEffect = emptyFlow(),
        onWatchedMovieListEvent = {},
        onMovieClicked = {},
    )
}
