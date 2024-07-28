package com.pantelisstampoulis.androidtemplateproject.network

import kotlinx.serialization.Serializable
import java.util.ArrayList

/**
 * Wrapper for data provided from the [BASE_URL]
 */
@Serializable
data class NetworkResponse<T>(
    val results: T,
)