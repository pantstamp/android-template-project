package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.discover

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.Preview
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.R
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.error.LoadError
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist.MovieListEvent
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist.MovieListScreen
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist.MovieListSideEffect
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist.MovieListUiState
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviesearch.MovieSearchEvent
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviesearch.MovieSearchResults
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviesearch.MovieSearchSideEffect
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviesearch.MovieSearchUiState
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.MovieUiModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.ObserveEffects
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * The Discover tab: a search field above either the Discover list or the search results.
 *
 * Both lists' scroll states are hoisted here, so each keeps its position while the other is
 * shown and across a trip to Movie Details. Both ViewModels' effects are observed here too,
 * because whichever list is hidden leaves composition.
 */
@Composable
fun DiscoverScreen(
    searchFieldState: TextFieldState,
    movieListState: MovieListUiState,
    movieListEffect: Flow<MovieListSideEffect>,
    onMovieListEvent: (MovieListEvent) -> Unit,
    searchState: MovieSearchUiState,
    searchEffect: Flow<MovieSearchSideEffect>,
    onSearchEvent: (MovieSearchEvent) -> Unit,
    onMovieClicked: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val coroutineScope = rememberCoroutineScope()
    val discoverListState = rememberLazyListState()
    val resultsListState = rememberLazyListState()
    // The query whose results were last scrolled to the top. Saveable, so that after process death
    // the re-run search does not scroll away from the restored position.
    var resultsScrolledFor by rememberSaveable { mutableStateOf<String?>(null) }

    // The field owns its text; the ViewModel decides what each change means. This re-sends the
    // current text after rotation (ignored as a repeat) and after process death (searches again).
    LaunchedEffect(searchFieldState) {
        snapshotFlow { searchFieldState.text.toString() }
            .collect { text -> onSearchEvent(MovieSearchEvent.QueryChanged(text)) }
    }

    HideKeyboardOnScroll(listState = discoverListState)
    HideKeyboardOnScroll(listState = resultsListState)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
    ) {
        MovieSearchField(state = searchFieldState)

        Box(modifier = Modifier.weight(1f)) {
            if (searchState.isActive) {
                MovieSearchResults(
                    state = searchState,
                    listState = resultsListState,
                    onEvent = onSearchEvent,
                )
            } else {
                MovieListScreen(
                    state = movieListState,
                    onEvent = onMovieListEvent,
                    lazyListState = discoverListState,
                )
            }
        }
    }

    ObserveEffects(effect = movieListEffect) { sideEffect ->
        when (sideEffect) {
            is MovieListSideEffect.RefreshFailed ->
                Toast.makeText(
                    context,
                    resources.getString(sideEffect.error.refreshFailedMessageRes()),
                    Toast.LENGTH_SHORT,
                )
                    .show()

            is MovieListSideEffect.NavigateToMovieDetails -> onMovieClicked(sideEffect.movieId)
        }
    }

    ObserveEffects(effect = searchEffect) { sideEffect ->
        when (sideEffect) {
            is MovieSearchSideEffect.RefreshFailed ->
                Toast.makeText(
                    context,
                    resources.getString(sideEffect.error.refreshFailedMessageRes()),
                    Toast.LENGTH_SHORT,
                )
                    .show()

            is MovieSearchSideEffect.NavigateToMovieDetails -> onMovieClicked(sideEffect.movieId)

            is MovieSearchSideEffect.ScrollResultsToTop -> {
                if (sideEffect.query != resultsScrolledFor) {
                    resultsScrolledFor = sideEffect.query
                    coroutineScope.launch { resultsListState.scrollToItem(0) }
                }
            }
        }
    }
}

@Composable
private fun HideKeyboardOnScroll(listState: LazyListState) {
    val focusManager = LocalFocusManager.current
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { it }
            .collect { focusManager.clearFocus() }
    }
}

private fun LoadError.refreshFailedMessageRes(): Int = when (this) {
    LoadError.Offline -> R.string.error_offline
    LoadError.Generic -> R.string.movie_list_error_generic
}

private val previewMovies = persistentListOf(
    MovieUiModel(
        id = 1,
        adult = false,
        backdropPath = null,
        genreStringId = R.string.genre_science_fiction,
        originalLanguage = "en",
        originalTitle = "The Matrix",
        overview = "A computer hacker learns the truth about his reality.",
        popularity = 87.0,
        posterPath = null,
        releaseYear = "1999",
        title = "The Matrix",
        video = false,
        voteAverage = 8.2,
        voteCount = 26380,
    ),
    MovieUiModel(
        id = 2,
        adult = false,
        backdropPath = null,
        genreStringId = R.string.genre_action,
        originalLanguage = "en",
        originalTitle = "The Matrix Reloaded",
        overview = "Neo and the rebels fight on.",
        popularity = 40.0,
        posterPath = null,
        releaseYear = "2003",
        title = "The Matrix Reloaded",
        video = false,
        voteAverage = 7.0,
        voteCount = 11000,
    ),
)

@Composable
private fun DiscoverPreview(query: String, searchState: MovieSearchUiState) {
    DiscoverScreen(
        searchFieldState = rememberTextFieldState(initialText = query),
        movieListState = MovieListUiState(data = previewMovies),
        movieListEffect = emptyFlow(),
        onMovieListEvent = {},
        searchState = searchState,
        searchEffect = emptyFlow(),
        onSearchEvent = {},
        onMovieClicked = {},
    )
}

@Preview
@Composable
fun PreviewDiscoverSearchInactive() {
    DiscoverPreview(query = "", searchState = MovieSearchUiState())
}

@Preview
@Composable
fun PreviewDiscoverSearchPending() {
    DiscoverPreview(
        query = "matrix",
        searchState = MovieSearchUiState(activeQuery = "matrix", isSearching = true),
    )
}

@Preview
@Composable
fun PreviewDiscoverSearchLoadingOverResults() {
    DiscoverPreview(
        query = "matrix",
        searchState = MovieSearchUiState(
            activeQuery = "matrix",
            results = previewMovies,
            resultsQuery = "mat",
            isSearching = true,
        ),
    )
}

@Preview
@Composable
fun PreviewDiscoverSearchResults() {
    DiscoverPreview(
        query = "matrix",
        searchState = MovieSearchUiState(activeQuery = "matrix", results = previewMovies, resultsQuery = "matrix"),
    )
}

@Preview
@Composable
fun PreviewDiscoverSearchNoResults() {
    DiscoverPreview(
        query = "zzzzqqq",
        searchState = MovieSearchUiState(
            activeQuery = "zzzzqqq",
            results = persistentListOf(),
            resultsQuery = "zzzzqqq",
        ),
    )
}

@Preview
@Composable
fun PreviewDiscoverSearchOffline() {
    DiscoverPreview(
        query = "matrix",
        searchState = MovieSearchUiState(activeQuery = "matrix", error = LoadError.Offline),
    )
}

@Preview
@Composable
fun PreviewDiscoverSearchGenericError() {
    DiscoverPreview(
        query = "matrix",
        searchState = MovieSearchUiState(activeQuery = "matrix", error = LoadError.Generic),
    )
}

@Preview
@Composable
fun PreviewDiscoverSearchRefreshing() {
    DiscoverPreview(
        query = "matrix",
        searchState = MovieSearchUiState(
            activeQuery = "matrix",
            results = previewMovies,
            resultsQuery = "matrix",
            isRefreshing = true,
        ),
    )
}
