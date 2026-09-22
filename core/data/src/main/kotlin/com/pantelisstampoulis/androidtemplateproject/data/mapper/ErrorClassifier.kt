package com.pantelisstampoulis.androidtemplateproject.data.mapper

import com.pantelisstampoulis.androidtemplateproject.model.error.DomainError
import com.pantelisstampoulis.androidtemplateproject.network.NetworkResult

internal class ErrorClassifier {

    fun toDomainError(result: NetworkResult<*>): DomainError? = when (result) {
        is NetworkResult.Error -> when (result.code) {
            400 -> DomainError.BadRequest(result.message)
            401 -> DomainError.Unauthorized(result.message)
            403 -> DomainError.Forbidden(result.message)
            404 -> DomainError.NotFound(result.message)
            500 -> DomainError.ServerError(result.message)
            503 -> DomainError.ServiceUnavailable(result.message)
            else -> DomainError.Unknown(result.message)
        }

        is NetworkResult.Exception -> when (val exception = result.exception) {
            is java.net.UnknownHostException -> DomainError.NoNetworkConnection("No internet connection")
            is java.net.SocketTimeoutException -> DomainError.ConnectionTimeout("Connection timed out")
            is java.net.ConnectException -> DomainError.ServerError("Unable to connect to the server")
            is javax.net.ssl.SSLException -> DomainError.ServerError("SSL error: ${exception.message}")
            is java.net.ProtocolException -> DomainError.ServerError("Protocol error: ${exception.message}")
            is java.io.IOException -> DomainError.ServerError("Network I/O error: ${exception.message}")
            else -> DomainError.Unknown("Unknown network error: ${exception.message}")
        }

        else -> null // In case of NetworkResult.Success, no error model is returned
    }
}
