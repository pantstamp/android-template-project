package com.pantelisstampoulis.androidtemplateproject.network.adapter

import com.pantelisstampoulis.androidtemplateproject.network.NetworkResult
import okhttp3.Request
import retrofit2.Callback
import okio.Timeout
import retrofit2.Call
import retrofit2.Response

class NetworkResultCall<T : Any>(
    private val delegate: Call<T>
) : Call<NetworkResult<T>> {

    override fun enqueue(callback: Callback<NetworkResult<T>>) {
        delegate.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val networkResult = if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        NetworkResult.Success(body)
                    } else {
                        NetworkResult.Error(response.code(), "Response body is null")
                    }
                } else {
                    NetworkResult.Error(response.code(), response.message())
                }
                callback.onResponse(this@NetworkResultCall, Response.success(networkResult))
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                val networkResult = NetworkResult.Exception(t)
                callback.onResponse(this@NetworkResultCall, Response.success(networkResult))
            }
        })
    }

    override fun clone(): Call<NetworkResult<T>> = NetworkResultCall(delegate.clone())
    override fun execute(): Response<NetworkResult<T>> = throw NotImplementedError()
    override fun isExecuted(): Boolean = delegate.isExecuted
    override fun cancel() = delegate.cancel()
    override fun isCanceled(): Boolean = delegate.isCanceled
    override fun request(): Request = delegate.request()
    override fun timeout(): Timeout = delegate.timeout()
}