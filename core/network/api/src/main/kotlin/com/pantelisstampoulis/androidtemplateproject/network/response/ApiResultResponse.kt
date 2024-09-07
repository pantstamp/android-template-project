package com.pantelisstampoulis.androidtemplateproject.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A data class representing the response from network operations that indicate success or failure.
 *
 * This class is used for API responses where the primary goal is to confirm whether an operation
 * was successful or not, without returning additional data. Typical use cases include operations
 * such as rating a movie, inserting a new object, or any other action that modifies data on the
 * backend.
 *
 * It leverages Kotlin's serialization library to facilitate seamless JSON parsing and serialization.
 *
 * @property success A boolean flag indicating whether the operation was successful.
 * @property statusCode An integer representing the status code returned by the server.
 * @property message A string providing a status message from the server, often used to convey
 * additional information about the success or failure of the operation.
 */
@Serializable
data class ApiResultResponse(
    val success: Boolean,
    @SerialName("status_code") val statusCode: Int,
    @SerialName("status_message") val message: String = "",
)
