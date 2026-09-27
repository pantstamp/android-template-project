package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.R
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.error.LoadError
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uicomponent.MovieRow
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uicomponent.PullToRefreshStatus
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uicomponent.StatusMessage
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uicomponent.iconRes
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uicomponent.listMessageRes
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.MovieUiModel
import com.pantelisstampoulis.androidtemplateproject.presentation.common.ui.uicomponent.PullToRefreshLazyColumn
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun MovieListScreen(
    state: MovieListUiState,
    onEvent: (MovieListEvent) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    // Effects are observed by DiscoverScreen: this composable leaves composition while a search
    // is shown, and Discover's effects must not wait for the search to be cleared.
    val movies = state.data
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        when {
            !movies.isNullOrEmpty() -> {
                MovieList(
                    movies = movies,
                    isRefreshing = state.isRefreshing,
                    onEvent = onEvent,
                    lazyListState = lazyListState,
                )
            }

            state.isLoading -> {
                CircularProgressIndicator()
            }

            state.error != null -> {
                MovieListStatus(
                    messageRes = state.error.listMessageRes(),
                    iconRes = state.error.iconRes(),
                    onRetry = { onEvent(MovieListEvent.Refresh) },
                )
            }

            movies != null -> {
                MovieListStatus(
                    messageRes = R.string.movie_list_empty,
                    iconRes = null,
                    onRetry = { onEvent(MovieListEvent.Refresh) },
                )
            }
        }
    }
}

@Composable
fun MovieList(
    movies: ImmutableList<MovieUiModel>,
    isRefreshing: Boolean,
    onEvent: (MovieListEvent) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    PullToRefreshLazyColumn(
        items = movies,
        key = { movie ->
            movie.id
        },
        content = { movie ->
            MovieRow(
                movie = movie,
                onClick = {
                    onEvent(MovieListEvent.ShowMovieDetails(movie.id))
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        },
        isRefreshing = isRefreshing,
        onRefresh = {
            onEvent(MovieListEvent.Refresh)
        },
        modifier = modifier,
        lazyListState = lazyListState,
    )
}

@Composable
private fun MovieListStatus(
    @StringRes messageRes: Int,
    @DrawableRes iconRes: Int?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshStatus(
        // A retry swaps this whole state for the full-screen spinner, so the indicator never shows.
        isRefreshing = false,
        onRefresh = onRetry,
        modifier = modifier,
    ) {
        StatusMessage(
            message = stringResource(id = messageRes),
            iconRes = iconRes,
            onRetry = onRetry,
        )
    }
}

private val previewMovie = MovieUiModel(
    id = 1,
    adult = false,
    backdropPath = null,
    genreStringId = R.string.genre_science_fiction,
    originalLanguage = "en",
    originalTitle = "Interstellar",
    overview = "A team of explorers travel through a wormhole in space.",
    popularity = 100.0,
    posterPath = null,
    releaseYear = "2014",
    title = "Interstellar",
    video = false,
    voteAverage = 8.6,
    voteCount = 30000,
)

@Preview
@Composable
fun PreviewMovieListOfflineError() {
    MovieListScreen(
        state = MovieListUiState(error = LoadError.Offline),
        onEvent = {},
        lazyListState = rememberLazyListState(),
    )
}

@Preview
@Composable
fun PreviewMovieListGenericError() {
    MovieListScreen(
        state = MovieListUiState(error = LoadError.Generic),
        onEvent = {},
        lazyListState = rememberLazyListState(),
    )
}

@Preview
@Composable
fun PreviewMovieListEmpty() {
    MovieListScreen(
        state = MovieListUiState(data = persistentListOf()),
        onEvent = {},
        lazyListState = rememberLazyListState(),
    )
}

@Preview
@Composable
fun PreviewMovieListLoading() {
    MovieListScreen(
        state = MovieListUiState(isLoading = true),
        onEvent = {},
        lazyListState = rememberLazyListState(),
    )
}

@Preview
@Composable
fun PreviewMovieListWithData() {
    MovieListScreen(
        state = MovieListUiState(data = persistentListOf(previewMovie, previewMovie.copy(id = 2, title = "Dune"))),
        onEvent = {},
        lazyListState = rememberLazyListState(),
    )
}

@Preview
@Composable
fun PreviewMovieListRefreshing() {
    MovieListScreen(
        state = MovieListUiState(
            isRefreshing = true,
            data = persistentListOf(previewMovie, previewMovie.copy(id = 2, title = "Dune")),
        ),
        onEvent = {},
        lazyListState = rememberLazyListState(),
    )
}
