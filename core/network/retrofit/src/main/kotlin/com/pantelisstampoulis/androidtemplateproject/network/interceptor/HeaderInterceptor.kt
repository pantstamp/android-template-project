package com.pantelisstampoulis.androidtemplateproject.network.interceptor

import com.pantelisstampoulis.androidtemplateproject.network.retrofit.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("accept", "application/json")
            .addHeader("Authorization", "Bearer ${BuildConfig.TMDB_API_KEY}")
            .build()
        return chain.proceed(request)
    }
}