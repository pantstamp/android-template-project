package com.pantelisstampoulis.androidtemplateproject.data.mapper.movie

import com.pantelisstampoulis.androidtemplateproject.network.IMAGE_URL

/** TMDB returns a relative poster path; the app stores and displays the full URL. */
internal fun String?.toPosterUrl(): String? = this?.let { IMAGE_URL + it }

/** TMDB sends "" for an unknown release date. */
internal fun String?.toReleaseDateOrNull(): String? = this?.takeIf { it.isNotBlank() }
