package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.pantelisstampoulis.androidtemplateproject.dispatcher.CoroutinesDispatchers
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.R
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.MovieUiModel
import com.pantelisstampoulis.androidtemplateproject.presentation.common.ui.uicomponent.PullToRefreshLazyColumn
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.ObserveEffects
import com.pantelisstampoulis.androidtemplateproject.presentation.theme.StarYellow
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import org.koin.compose.getKoin
import org.koin.core.qualifier.named
import kotlin.coroutines.CoroutineContext

@Composable
fun MovieListScreen(
    state: MovieListUiState,
    effect: Flow<MovieListSideEffect>,
    onEvent: (MovieListEvent) -> Unit,
    onMovieClicked: (Int) -> Unit,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val movies = state.data
    Box(
        modifier = Modifier
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

        ObserveEffects(
            effect = effect,
            coroutineContext = getKoin().get<CoroutineContext>(named(CoroutinesDispatchers.MainImmediate)),
            lifecycleOwner = LocalLifecycleOwner.current,
        ) { sideEffect ->
            when (sideEffect) {
                is MovieListSideEffect.RefreshFailed ->
                    Toast.makeText(context, resources.getString(sideEffect.error.messageRes()), Toast.LENGTH_SHORT)
                        .show()

                is MovieListSideEffect.NavigateToMovieDetails -> {
                    onMovieClicked(sideEffect.movieId)
                }
            }
        }
    }
}

@Composable
fun MovieList(
    movies: ImmutableList<MovieUiModel>,
    isRefreshing: Boolean,
    onEvent: (MovieListEvent) -> Unit,
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
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieListStatus(
    @StringRes messageRes: Int,
    @DrawableRes iconRes: Int?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        // A retry swaps this whole state for the full-screen spinner, so the indicator never shows.
        isRefreshing = false,
        onRefresh = onRetry,
        modifier = modifier.fillMaxSize(),
    ) {
        // PullToRefreshBox only reacts to nested scroll, so the content must be scrollable.
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Column(
                    modifier = Modifier
                        .fillParentMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (iconRes != null) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Text(
                        text = stringResource(id = messageRes),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onRetry) {
                        Text(text = stringResource(id = R.string.action_retry))
                    }
                }
            }
        }
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
            AsyncImage(
                model = movie.posterPath,
                contentDescription = null,
                modifier = Modifier.fillMaxHeight(),
            )

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

@StringRes
private fun MovieListError.messageRes(): Int = when (this) {
    MovieListError.Offline -> R.string.movie_list_error_offline
    MovieListError.Generic -> R.string.movie_list_error_generic
}

@DrawableRes
private fun MovieListError.iconRes(): Int = when (this) {
    MovieListError.Offline -> R.drawable.ic_cloud_off
    MovieListError.Generic -> R.drawable.ic_error
}
