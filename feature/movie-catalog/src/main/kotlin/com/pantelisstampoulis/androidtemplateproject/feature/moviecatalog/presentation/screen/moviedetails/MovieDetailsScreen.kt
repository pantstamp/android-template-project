package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviedetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.pantelisstampoulis.androidtemplateproject.dispatcher.CoroutinesDispatchers
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.R
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uicomponent.UserRatingBar
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.MovieUiModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.ObserveEffects
import com.pantelisstampoulis.androidtemplateproject.presentation.theme.StarYellow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.compose.getKoin
import org.koin.core.qualifier.named
import kotlin.coroutines.CoroutineContext

@Composable
fun MovieDetailsScreen(
    state: MovieDetailsUiState,
    effect: Flow<MovieDetailsSideEffect>,
    onEvent: (MovieDetailsEvent) -> Unit,
    movieId: Int,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                }

                state.errorMessage != null -> {
                    Text(
                        text = state.errorMessage,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                state.data != null -> {
                    MovieDetails(
                        movie = state.data,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onEvent = onEvent,
                        userRating = state.userRating,
                        isRatingInProgress = state.isRatingInProgress,
                    )
                }
            }

            LifecycleEventEffect(event = Lifecycle.Event.ON_CREATE) {
                onEvent(MovieDetailsEvent.Init(movieId))
            }

            ObserveEffects(
                effect = effect,
                coroutineContext = getKoin().get<CoroutineContext>(named(CoroutinesDispatchers.MainImmediate)),
                lifecycleOwner = LocalLifecycleOwner.current,
            ) { sideEffect ->
                when (sideEffect) {
                    is MovieDetailsSideEffect.ShowSnackbar ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(sideEffect.message)
                        }
                }
            }
        }
    }
}

@Composable
fun MovieDetails(
    movie: MovieUiModel,
    modifier: Modifier = Modifier,
    onEvent: (MovieDetailsEvent) -> Unit,
    userRating: Int?,
    isRatingInProgress: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = 6.dp),
    ) {
        AsyncImage(
            model = movie.posterPath,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = movie.title,
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            text = movie.overview,
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Text(
                text = stringResource(id = movie.genreStringId),
                style = MaterialTheme.typography.labelMedium,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = movie.releaseYear,
                style = MaterialTheme.typography.labelMedium,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_star),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = StarYellow,
                )

                Text(
                    text = movie.voteAverage.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        RateMovie(
            modifier = modifier,
            onEvent = onEvent,
            movie = movie,
            userRating = userRating,
            isRatingInProgress = isRatingInProgress,
        )
    }
}

@Composable
fun RateMovie(
    modifier: Modifier = Modifier,
    onEvent: (MovieDetailsEvent) -> Unit,
    movie: MovieUiModel,
    userRating: Int?,
    isRatingInProgress: Boolean,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (userRating != null) {
            Text(
                text = stringResource(id = R.string.label_you_rated_this),
                style = MaterialTheme.typography.labelMedium,
            )
            val lockedRatingState = remember { mutableIntStateOf(userRating) }
            UserRatingBar(
                ratingState = lockedRatingState,
                size = 28.dp,
                enabled = false,
            )
        } else {
            val ratingState = rememberSaveable { mutableIntStateOf(0) }
            UserRatingBar(
                ratingState = ratingState,
                size = 28.dp,
                enabled = !isRatingInProgress,
            )
            Button(
                onClick = {
                    onEvent(MovieDetailsEvent.RateMovie(movie.id, ratingState.intValue.toFloat()))
                },
                enabled = ratingState.intValue >= 1 && !isRatingInProgress,
            ) {
                Text(
                    text = stringResource(id = R.string.label_rate),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
