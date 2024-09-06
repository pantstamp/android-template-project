package com.pantelisstampoulis.androidtemplateproject.network

sealed class NetworkResult<out T : Any> {
    data class Success<out T : Any>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int, val message: String?) : NetworkResult<Nothing>()
    data class Exception(val exception: Throwable) : NetworkResult<Nothing>()
}

/**
 * Extension function to check if a [NetworkResult] is a success.
 *
 * @return `true` if the [NetworkResult] is of type [NetworkResult.Success], `false` otherwise.
 */
fun <T : Any> NetworkResult<T>.isSuccess(): Boolean = this is NetworkResult.Success
