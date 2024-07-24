package com.pantelisstampoulis.androidtemplateproject.network

import kotlinx.serialization.Serializable

/**
 * Wrapper for data provided from the [BASE_URL]
 */
@Serializable
data class NetworkResponse<T>(
    val data: T,
)