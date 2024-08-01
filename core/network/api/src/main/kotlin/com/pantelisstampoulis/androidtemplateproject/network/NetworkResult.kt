package com.pantelisstampoulis.androidtemplateproject.network

sealed class NetworkResult<out T : Any> {
    data class Success<out T : Any>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int, val message: String?) : NetworkResult<Nothing>()
    data class Exception(val exception: Throwable) : NetworkResult<Nothing>()
}