package com.pantelisstampoulis.androidtemplateproject.network.adapter

import com.pantelisstampoulis.androidtemplateproject.network.NetworkResult
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

class NetworkResultCallAdapter<R : Any>(
    private val responseType: Type,
) : CallAdapter<R, Call<NetworkResult<R>>> {

    override fun responseType(): Type = responseType

    override fun adapt(call: Call<R>): Call<NetworkResult<R>> = NetworkResultCall(call)
}
