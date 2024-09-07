package com.pantelisstampoulis.androidtemplateproject.network.adapter

import com.pantelisstampoulis.androidtemplateproject.network.NetworkResult
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class NetworkResultCallAdapterFactory private constructor() : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): CallAdapter<*, *>? {
        // Check if the return type is a Call<T>
        if (getRawType(returnType) != Call::class.java) {
            return null
        }

        // Extract the inner type, should be NetworkResult<T>
        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        if (getRawType(callType) != NetworkResult::class.java) {
            return null
        }

        // Extract the type contained within NetworkResult<T>
        val resultType = getParameterUpperBound(0, callType as ParameterizedType)

        // Pass the resultType explicitly to NetworkResultCallAdapter
        return NetworkResultCallAdapter<Any>(resultType)
    }

    companion object {
        fun create(): NetworkResultCallAdapterFactory = NetworkResultCallAdapterFactory()
    }
}
