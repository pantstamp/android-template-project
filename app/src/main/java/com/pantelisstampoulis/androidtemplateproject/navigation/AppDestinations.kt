package com.pantelisstampoulis.androidtemplateproject.navigation

import kotlinx.serialization.Serializable

sealed interface AppDestination {

    @Serializable
    data object HomeScreenDestination: AppDestination
}