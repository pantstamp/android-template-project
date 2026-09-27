package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviesearch

import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.error.LoadError
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.SideEffect

sealed interface MovieSearchSideEffect : SideEffect {

    data class NavigateToMovieDetails(val movieId: Int) : MovieSearchSideEffect

    data class RefreshFailed(val error: LoadError) : MovieSearchSideEffect

    /** Results for a different query than the ones on screen have arrived. */
    data class ScrollResultsToTop(val query: String) : MovieSearchSideEffect
}
