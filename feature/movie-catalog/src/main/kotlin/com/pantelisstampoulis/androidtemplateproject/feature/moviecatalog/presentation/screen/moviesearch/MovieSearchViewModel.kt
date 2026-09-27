package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviesearch

import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.onError
import com.pantelisstampoulis.androidtemplateproject.domain.onSuccess
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.SearchMoviesUseCase
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.error.LoadError
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.error.toLoadError
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper.MovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.MovieUiModel
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.MviViewModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

/**
 * Searches TMDB by title as the user types. The field's text is owned by the UI; this ViewModel
 * receives every change as [MovieSearchEvent.QueryChanged] and owns what the change means: the
 * minimum length, the pause before searching, and which results may reach the screen.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class MovieSearchViewModel(
    private val searchMoviesUseCase: SearchMoviesUseCase,
    private val mapper: MovieUiMapper,
    private val debounceMillis: Long = SEARCH_DEBOUNCE_MILLIS,
) : MviViewModel<MovieSearchEvent, MovieSearchUiState, MovieSearchSideEffect>(
    initialState = MovieSearchUiState(),
) {

    // Declared before init: property initialisers and init blocks run in declaration order.
    private val activeQueries = MutableStateFlow(ActiveQuery(text = "", generation = 0))

    // The newest request always wins, matching flatMapLatest downstream; a full buffer can
    // never silently drop the request whose flags were just set.
    private val reruns = MutableSharedFlow<SearchRequest>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        viewModelScope.launch {
            merge(
                activeQueries
                    // Clearing is instant, so a stale search cannot land after Discover is back.
                    .debounce { if (it.text.isEmpty()) 0L else debounceMillis }
                    .map { SearchRequest(query = it.text, kind = SearchKind.New) },
                reruns,
            )
                // A newer request cancels the running search, so stale results are never applied.
                .flatMapLatest { request ->
                    if (request.query.isEmpty()) {
                        emptyFlow()
                    } else {
                        searchMoviesUseCase(request.query).map { result -> request to result }
                    }
                }
                .collect { (request, result) -> reduce(request, result) }
        }
    }

    override fun handleEvents(event: MovieSearchEvent) {
        when (event) {
            is MovieSearchEvent.QueryChanged -> onQueryChanged(event.text)

            MovieSearchEvent.Retry -> retry()

            MovieSearchEvent.Refresh -> refresh()

            is MovieSearchEvent.ShowMovieDetails -> setEffect {
                MovieSearchSideEffect.NavigateToMovieDetails(event.movieId)
            }
        }
    }

    private fun onQueryChanged(text: String) {
        val trimmed = text.trim()
        val active = if (trimmed.length >= MIN_QUERY_LENGTH) trimmed else ""
        val current = activeQueries.value
        // Same text re-sent after rotation or a tab switch: nothing to do.
        if (active == current.text) return
        // The generation keeps every change distinct. StateFlow skips a value equal to the last one
        // its collector saw, so "matrix" -> "" -> "matrix" handled before the pipeline runs would
        // otherwise never search again, leaving the spinner up.
        activeQueries.value = ActiveQuery(text = active, generation = current.generation + 1)
        setState {
            if (active.isEmpty()) {
                // Back to Discover immediately; forget the results.
                MovieSearchUiState()
            } else {
                // Previous results stay on screen while the new search is pending.
                copy(activeQuery = active, isSearching = true, isRefreshing = false, error = null)
            }
        }
    }

    private fun retry() {
        val query = viewState.value.activeQuery
        if (query.isEmpty()) return
        setState { copy(isSearching = true, error = null) }
        reruns.tryEmit(SearchRequest(query = query, kind = SearchKind.Retry))
    }

    private fun refresh() {
        val state = viewState.value
        // A search for activeQuery is already pending or running; a refresh would only repeat it.
        if (!state.isActive || state.isSearching) return
        if (state.results == null) {
            retry()
            return
        }
        setState { copy(isRefreshing = true) }
        reruns.tryEmit(SearchRequest(query = state.activeQuery, kind = SearchKind.Refresh))
    }

    private fun reduce(request: SearchRequest, result: ResultState<List<Movie>>) {
        // A result for a query that is no longer active (e.g. it landed just as the field was
        // cleared, before cancellation reached it) must not reach the screen.
        if (request.query != viewState.value.activeQuery) return
        result
            .onSuccess { movies ->
                val isNewQuery = viewState.value.resultsQuery != request.query
                setState {
                    copy(
                        results = movies.map(mapper::fromDomainToUi).toImmutableList(),
                        resultsQuery = request.query,
                        isSearching = false,
                        isRefreshing = false,
                        error = null,
                    )
                }
                if (isNewQuery) {
                    setEffect { MovieSearchSideEffect.ScrollResultsToTop(request.query) }
                }
            }
            .onError { domainError ->
                val error = domainError.toLoadError()
                val keepResults = request.kind == SearchKind.Refresh &&
                    viewState.value.resultsQuery == request.query
                if (keepResults) {
                    // The results still match the query; a failed refresh is a side effect.
                    setState { copy(isSearching = false, isRefreshing = false) }
                    setEffect { MovieSearchSideEffect.RefreshFailed(error) }
                } else {
                    setState {
                        copy(
                            results = null,
                            resultsQuery = null,
                            isSearching = false,
                            isRefreshing = false,
                            error = error,
                        )
                    }
                }
            }
    }

    private data class ActiveQuery(
        val text: String,
        val generation: Int,
    )

    private data class SearchRequest(
        val query: String,
        val kind: SearchKind,
    )

    private enum class SearchKind { New, Retry, Refresh }

    companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
        const val MIN_QUERY_LENGTH = 2
    }
}

data class MovieSearchUiState(
    /** Trimmed query that is in effect; "" means search is inactive and Discover is shown. */
    val activeQuery: String = "",
    /** Results on screen, and the query they belong to. Null until a search has succeeded. */
    val results: ImmutableList<MovieUiModel>? = null,
    val resultsQuery: String? = null,
    /** A search for [activeQuery] is waiting for the debounce or is running. */
    val isSearching: Boolean = false,
    val isRefreshing: Boolean = false,
    /** Set only when there are no results to show for [activeQuery]. */
    val error: LoadError? = null,
) : UiState {
    val isActive: Boolean get() = activeQuery.isNotEmpty()
}
