package com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.presentation.screen.movie_details

import android.widget.Toast
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.pantelisstampoulis.androidtemplateproject.dispatcher.CoroutinesDispatchers
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.presentation.ui_components.UserRatingBar
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.presentation.ui_model.MovieUiModel
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.R
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.ObserveEffects
import com.pantelisstampoulis.androidtemplateproject.theme.StarYellow
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
                    onEvent = onEvent
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
            when (sideEffect) {
                is MovieDetailsSideEffect.ShowToast ->
                    Toast.makeText(context, sideEffect.text, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun MovieDetails(
    movie: MovieUiModel,
    modifier: Modifier = Modifier,
    onEvent: (MovieDetailsEvent) -> Unit,
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
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = movie.title,
            style = MaterialTheme.typography.titleMedium,
        )


        Text(
            text = movie.overview,
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_star),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = StarYellow
            )

            Text(
                text = movie.voteAverage.toString(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        RateMovie(
            modifier = modifier,
            onEvent = onEvent,
            movie = movie
        )
    }

}



@Composable
fun RateMovie(
    modifier: Modifier = Modifier,
    onEvent: (MovieDetailsEvent) -> Unit,
    movie: MovieUiModel,
) {
    val ratingState = remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize(), // Wrap the content size to center horizontally
        verticalArrangement = Arrangement.spacedBy(16.dp), // Space between items
        horizontalAlignment = Alignment.CenterHorizontally // Center the items horizontally
    ) {
        UserRatingBar(
            ratingState = ratingState,
            size = 28.dp,
        )

        Button(onClick = {
            onEvent(MovieDetailsEvent.RateMovie(movie.id, ratingState.intValue.toFloat()))
        }) {
            Text(
                text = stringResource(id = R.string.label_rate),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}


