package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.R
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.discover.DiscoverScreen
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist.MovieListEvent
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist.MovieListSideEffect
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist.MovieListUiState
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviesearch.MovieSearchEvent
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviesearch.MovieSearchSideEffect
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviesearch.MovieSearchUiState
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.watchedmovielist.WatchedMovieListEvent
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.watchedmovielist.WatchedMovieListScreen
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.watchedmovielist.WatchedMovieListSideEffect
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.watchedmovielist.WatchedMovieListUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@Composable
fun MovieCatalogTabbedScreen(
    movieListState: MovieListUiState,
    movieListEffect: Flow<MovieListSideEffect>,
    onMovieListEvent: (MovieListEvent) -> Unit,
    searchState: MovieSearchUiState,
    searchEffect: Flow<MovieSearchSideEffect>,
    onSearchEvent: (MovieSearchEvent) -> Unit,
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
    val focusManager = LocalFocusManager.current
    // Hoisted above the pager so the back handler can see it. Saveable: survives rotation and
    // process death, while a fresh launch starts empty.
    val searchFieldState = rememberTextFieldState()

    // Back clears an active search before it leaves the app. The page check is required: the
    // pager keeps Discover composed while Watched is shown. An open keyboard takes the first
    // back press itself.
    BackHandler(enabled = pagerState.currentPage == DISCOVER_PAGE && searchFieldState.text.isNotEmpty()) {
        searchFieldState.clearText()
    }

    // Switching tabs hides the keyboard; the search itself is kept.
    LaunchedEffect(pagerState.currentPage) {
        focusManager.clearFocus()
    }

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
                DISCOVER_PAGE -> DiscoverScreen(
                    searchFieldState = searchFieldState,
                    movieListState = movieListState,
                    movieListEffect = movieListEffect,
                    onMovieListEvent = onMovieListEvent,
                    searchState = searchState,
                    searchEffect = searchEffect,
                    onSearchEvent = onSearchEvent,
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

private const val DISCOVER_PAGE = 0

@Preview
@Composable
fun PreviewMovieCatalogTabbedScreen() {
    MovieCatalogTabbedScreen(
        movieListState = MovieListUiState(isLoading = true),
        movieListEffect = emptyFlow(),
        onMovieListEvent = {},
        searchState = MovieSearchUiState(),
        searchEffect = emptyFlow(),
        onSearchEvent = {},
        watchedMovieListState = WatchedMovieListUiState(),
        watchedMovieListEffect = emptyFlow(),
        onWatchedMovieListEvent = {},
        onMovieClicked = {},
    )
}
