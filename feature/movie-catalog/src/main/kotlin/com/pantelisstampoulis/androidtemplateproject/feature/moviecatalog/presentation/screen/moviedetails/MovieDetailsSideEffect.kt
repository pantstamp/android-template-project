package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviedetails

import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.SideEffect

sealed interface MovieDetailsSideEffect : SideEffect {
    data class ShowToast(val text: String) : MovieDetailsSideEffect
}
