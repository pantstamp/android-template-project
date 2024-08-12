package com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.presentation.screen.movie_details

import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.SideEffect


sealed interface MovieDetailsSideEffect : SideEffect {
    data class ShowToast(val text: String) : MovieDetailsSideEffect

}
