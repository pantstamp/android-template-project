package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.R
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.error.LoadError
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uicomponent.PullToRefreshStatus
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uicomponent.StatusMessage
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uicomponent.iconRes
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.MovieUiModel
import com.pantelisstampoulis.androidtemplateproject.presentation.common.ui.uicomponent.PullToRefreshLazyColumn
import com.pantelisstampoulis.androidtemplateproject.presentation.theme.StarYellow
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
                    messageRes = state.error.messageRes(),
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

@Composable
fun MovieRow(movie: MovieUiModel, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(150.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
        ) {
            MoviePoster(posterUrl = movie.posterPath)

            Column(
                modifier = Modifier
                    .padding(all = 16.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween, // Ensures space between title and rating
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(id = movie.genreStringId),
                        style = MaterialTheme.typography.labelMedium,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = movie.releaseYear,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End, // Aligns items to the end of the Row
                    modifier = Modifier.fillMaxWidth(), // Ensures the Row takes the full width
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = StarYellow,
                    )

                    Text(
                        text = movie.voteAverage.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
        }
    }
}

/**
 * A fixed 2:3 poster area. The placeholder icon sits under the image: while the poster loads,
 * when it fails, or when there is none, [AsyncImage] draws nothing and the icon shows; a loaded
 * poster is opaque and covers it. Layering avoids SubcomposeAsyncImage, which Coil advises
 * against in lazy lists.
 */
@Composable
private fun MoviePoster(posterUrl: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(2f / 3f)
            .background(color = MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_movie_placeholder),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp),
        )
        AsyncImage(
            model = posterUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
    }
}

@StringRes
private fun LoadError.messageRes(): Int = when (this) {
    LoadError.Offline -> R.string.error_offline
    LoadError.Generic -> R.string.movie_list_error_generic
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
fun PreviewMovieRowPosterPlaceholder() {
    MovieRow(movie = previewMovie.copy(posterPath = null), onClick = {})
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
