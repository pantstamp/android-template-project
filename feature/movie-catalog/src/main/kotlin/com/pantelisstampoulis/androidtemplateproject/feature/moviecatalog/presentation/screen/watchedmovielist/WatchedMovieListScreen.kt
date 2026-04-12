package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.watchedmovielist

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.pantelisstampoulis.androidtemplateproject.dispatcher.CoroutinesDispatchers
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.R
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.WatchedMovieUiModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.ObserveEffects
import com.pantelisstampoulis.androidtemplateproject.presentation.theme.StarYellow
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.compose.getKoin
import org.koin.core.qualifier.named
import kotlin.coroutines.CoroutineContext

@Composable
fun WatchedMovieListScreen(
    state: WatchedMovieListUiState,
    effect: Flow<WatchedMovieListSideEffect>,
    onEvent: (WatchedMovieListEvent) -> Unit,
    onMovieClicked: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator()
            }

            state.errorRes != null -> {
                Text(
                    text = stringResource(id = state.errorRes),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            state.data != null -> {
                if (state.data.isEmpty()) {
                    Text(text = stringResource(id = R.string.watched_empty_state))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().align(Alignment.TopStart)) {
                        items(state.data, key = { it.movieId }) { movie ->
                            WatchedMovieRow(
                                movie = movie,
                                onClick = { onEvent(WatchedMovieListEvent.ShowMovieDetails(movie.movieId)) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }
                }
            }
        }

        ObserveEffects(
            effect = effect,
            coroutineContext = getKoin().get<CoroutineContext>(named(CoroutinesDispatchers.MainImmediate)),
            lifecycleOwner = LocalLifecycleOwner.current,
        ) { sideEffect ->
            when (sideEffect) {
                is WatchedMovieListSideEffect.NavigateToMovieDetails ->
                    onMovieClicked(sideEffect.movieId)
            }
        }
    }
}

@Composable
fun WatchedMovieRow(
    movie: WatchedMovieUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = movie.releaseYear,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
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

                    Spacer(modifier = Modifier.weight(1f))

                    val userRatingDescription = stringResource(
                        R.string.content_description_user_rating,
                        movie.userRating,
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = null,
                        modifier = Modifier
                            .size(28.dp)
                            .semantics { contentDescription = userRatingDescription },
                        tint = StarYellow,
                    )

                    Text(
                        text = movie.userRating.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
        }
    }
}

private val previewMovie = WatchedMovieUiModel(
    movieId = 1,
    title = "Interstellar",
    posterPath = null,
    voteAverage = 8.6,
    userRating = 9,
    releaseYear = "2014",
)

@Preview
@Composable
fun PreviewWatchedMovieRow() {
    WatchedMovieRow(
        movie = previewMovie,
        onClick = {},
    )
}

@Preview
@Composable
fun PreviewWatchedMovieListLoading() {
    WatchedMovieListScreen(
        state = WatchedMovieListUiState(isLoading = true),
        effect = emptyFlow(),
        onEvent = {},
        onMovieClicked = {},
    )
}

@Preview
@Composable
fun PreviewWatchedMovieListEmpty() {
    WatchedMovieListScreen(
        state = WatchedMovieListUiState(data = persistentListOf()),
        effect = emptyFlow(),
        onEvent = {},
        onMovieClicked = {},
    )
}

@Preview
@Composable
fun PreviewWatchedMovieListWithData() {
    WatchedMovieListScreen(
        state = WatchedMovieListUiState(
            data = persistentListOf(previewMovie, previewMovie.copy(movieId = 2, title = "Dune")),
        ),
        effect = emptyFlow(),
        onEvent = {},
        onMovieClicked = {},
    )
}
