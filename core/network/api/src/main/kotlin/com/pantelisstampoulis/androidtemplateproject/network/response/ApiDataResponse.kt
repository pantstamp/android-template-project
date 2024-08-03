package com.pantelisstampoulis.androidtemplateproject.network.response

import kotlinx.serialization.Serializable

/**
 * A generic wrapper class for API responses from the backend.
 *
 * This data class is used to encapsulate the data returned by API calls made to the backend,
 * providing a consistent structure for handling responses that include a `results` field.
 * It utilizes Kotlin's serialization library to facilitate easy JSON parsing.
 *
 * @param T The type of data contained within the `results` field. This allows for flexibility
 * in handling various types of data returned by different API endpoints.
 */
@Serializable
data class ApiDataResponse<T>(
    val results: T,
)
