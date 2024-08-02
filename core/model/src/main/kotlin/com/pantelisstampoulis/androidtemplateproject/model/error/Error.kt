package com.pantelisstampoulis.androidtemplateproject.model.error

sealed class Error(val message: String? = null) {
    // Error type for no network connection with an optional message
    data class NoNetworkConnection(val errorMessage: String? = null) : Error(errorMessage)

    // Error type for connection timeout with an optional message
    data class ConnectionTimeout(val errorMessage: String? = null) : Error(errorMessage)

    // Error type for server error with an optional message
    data class ServerError(val errorMessage: String? = null) : Error(errorMessage)

    // Error type for unauthorized access with an optional message
    data class Unauthorized(val errorMessage: String? = null) : Error(errorMessage)

    // Error type for forbidden access with an optional message
    data class Forbidden(val errorMessage: String? = null) : Error(errorMessage)

    // Error type for bad request with an optional message
    data class BadRequest(val errorMessage: String? = null) : Error(errorMessage)

    // Error type for not found with an optional message
    data class NotFound(val errorMessage: String? = null) : Error(errorMessage)

    // Error type for service unavailable with an optional message
    data class ServiceUnavailable(val errorMessage: String? = null) : Error(errorMessage)

    // Error type for unknown error with an optional message
    data class Unknown(val errorMessage: String? = null) : Error(errorMessage)
}