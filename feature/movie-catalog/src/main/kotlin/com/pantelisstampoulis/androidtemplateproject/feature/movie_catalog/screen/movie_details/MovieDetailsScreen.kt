package com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.screen.movie_details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.ui_model.MovieUiModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.ObserveEffects
import kotlinx.coroutines.flow.Flow
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
                MovieDetails(
                    movie = state.data,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        LifecycleEventEffect(event = Lifecycle.Event.ON_CREATE) {
            onEvent(MovieDetailsEvent.Init(movieId))
        }

        ObserveEffects(
            effect = effect,
            coroutineContext = getKoin().get<CoroutineContext>(named(CoroutinesDispatchers.MainImmediate)),
            lifecycleOwner = LocalLifecycleOwner.current
        ) { sideEffect ->
           // do nothing
        }
    }
}

@Composable
fun MovieDetails(
    movie: MovieUiModel,
    modifier: Modifier = Modifier,
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
            text = movie.overview,
        )

        Text(
            text = movie.voteAverage.toString(),
        )

        RateMovie(
            modifier = modifier
        )
    }

}

@Composable
fun RateMovie(
    modifier: Modifier = Modifier
) {
    Button(onClick = { /*TODO*/ }) {
        Text(text = "Rate")
    }
}


