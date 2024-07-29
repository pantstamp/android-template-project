package com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.screen.movie_list

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pantelisstampoulis.androidtemplateproject.dispatcher.CoroutinesDispatchers
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.ObserveEffects
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
) {
    val context = LocalContext.current
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

            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            state.data != null -> {
                MovieList(
                    movies = state.data,
                    onEvent = onEvent,
                )
            }
        }

        LifecycleEventEffect(event = Lifecycle.Event.ON_CREATE) {
            onEvent(MovieListEvent.Init)
        }

        ObserveEffects(
            effect = effect,
            coroutineContext = getKoin().get<CoroutineContext>(named(CoroutinesDispatchers.MainImmediate)),
            lifecycleOwner = LocalLifecycleOwner.current
        ) { sideEffect ->
            when (sideEffect) {
                is MovieListSideEffect.ShowToast ->
                    Toast.makeText(context, sideEffect.text, Toast.LENGTH_SHORT).show()

                is MovieListSideEffect.NavigateToMovieDetails -> {
                    //navController.navigate(Routes.accountDetailsRoute(sideEffect.accountId))
                }
            }
        }
    }
}

@Composable
fun MovieList(
    movies: ImmutableList<MovieUiModel>,
    onEvent: (MovieListEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
    ) {
        items(
            items = movies,
            key = { movie -> movie.id },
        ) { movie ->
            MovieRow(
                movie = movie,
                onClick = {
                    onEvent(MovieListEvent.ShowMovieDetails(movie.id))
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

// @OptIn(ExperimentalMaterialApi::class)
@Composable
fun MovieRow(
    movie: MovieUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = movie.title,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = movie.voteAverage.toString(),
            )
        }
    }
}


