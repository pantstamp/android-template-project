package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.error

import com.pantelisstampoulis.androidtemplateproject.model.error.DomainError

/** Why a screen has nothing to show. Each screen resolves its own message text. */
enum class LoadError { Offline, Generic }

internal fun DomainError.toLoadError(): LoadError = when (this) {
    is DomainError.NoNetworkConnection -> LoadError.Offline
    else -> LoadError.Generic
}
