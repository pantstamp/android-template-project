package com.pantelisstampoulis.androidtemplateproject.data.mapper

import com.pantelisstampoulis.androidtemplateproject.model.error.ErrorModel
import com.pantelisstampoulis.androidtemplateproject.network.NetworkResult

internal class ErrorDomainMapper {

    fun mapNetworkResultToErrorModel(result: NetworkResult<*>): ErrorModel? = when (result) {
        is NetworkResult.Error -> when (result.code) {
            400 -> ErrorModel.BadRequest(result.message)
            401 -> ErrorModel.Unauthorized(result.message)
            403 -> ErrorModel.Forbidden(result.message)
            404 -> ErrorModel.NotFound(result.message)
            500 -> ErrorModel.ServerError(result.message)
            503 -> ErrorModel.ServiceUnavailable(result.message)
            else -> ErrorModel.Unknown(result.message)
        }

        is NetworkResult.Exception -> when (val exception = result.exception) {
            is java.net.UnknownHostException -> ErrorModel.NoNetworkConnection("No internet connection")
            is java.net.SocketTimeoutException -> ErrorModel.ConnectionTimeout("Connection timed out")
            is java.net.ConnectException -> ErrorModel.ServerError("Unable to connect to the server")
            is javax.net.ssl.SSLException -> ErrorModel.ServerError("SSL error: ${exception.message}")
            is java.net.ProtocolException -> ErrorModel.ServerError("Protocol error: ${exception.message}")
            is java.io.IOException -> ErrorModel.ServerError("Network I/O error: ${exception.message}")
            else -> ErrorModel.Unknown("Unknown network error: ${exception.message}")
        }

        else -> null // In case of NetworkResult.Success, no error model is returned
    }
}
