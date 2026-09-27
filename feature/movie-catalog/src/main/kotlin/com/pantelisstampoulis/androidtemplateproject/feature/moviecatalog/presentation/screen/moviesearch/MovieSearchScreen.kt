package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviesearch

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.R
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uicomponent.MovieRow
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uicomponent.PullToRefreshStatus
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uicomponent.StatusMessage
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uicomponent.iconRes
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uicomponent.listMessageRes
import com.pantelisstampoulis.androidtemplateproject.presentation.common.ui.uicomponent.PullToRefreshLazyColumn

/** Search results in place of the Discover list, in whichever state the search is in. */
@Composable
fun MovieSearchScreen(
    state: MovieSearchUiState,
    listState: LazyListState,
    onEvent: (MovieSearchEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val results = state.results
        val error = state.error
        when {
            results != null -> {
                if (results.isNotEmpty()) {
                    PullToRefreshLazyColumn(
                        items = results,
                        key = { movie -> movie.id },
                        content = { movie ->
                            MovieRow(
                                movie = movie,
                                onClick = { onEvent(MovieSearchEvent.ShowMovieDetails(movie.id)) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        },
                        isRefreshing = state.isRefreshing,
                        onRefresh = { onEvent(MovieSearchEvent.Refresh) },
                        lazyListState = listState,
                    )
                } else {
                    PullToRefreshStatus(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { onEvent(MovieSearchEvent.Refresh) },
                    ) {
                        StatusMessage(
                            message = stringResource(id = R.string.search_no_results, state.resultsQuery.orEmpty()),
                            iconRes = null,
                            onRetry = null,
                        )
                    }
                }
                // Results for the previous query stay visible while the next one loads.
                if (state.isSearching) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                    )
                }
            }

            error != null -> {
                PullToRefreshStatus(
                    // A retry swaps this state for the spinner, so the indicator never shows.
                    isRefreshing = false,
                    onRefresh = { onEvent(MovieSearchEvent.Refresh) },
                ) {
                    StatusMessage(
                        message = stringResource(id = error.listMessageRes()),
                        iconRes = error.iconRes(),
                        onRetry = { onEvent(MovieSearchEvent.Retry) },
                    )
                }
            }

            else -> CircularProgressIndicator()
        }
    }
}
