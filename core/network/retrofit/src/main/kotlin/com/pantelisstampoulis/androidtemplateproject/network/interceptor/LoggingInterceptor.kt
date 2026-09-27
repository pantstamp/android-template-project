package com.pantelisstampoulis.androidtemplateproject.network.interceptor

import com.pantelisstampoulis.androidtemplateproject.network.retrofit.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Logs request and response bodies in debug builds only, and never prints the
 * `Authorization` header: [HeaderInterceptor] puts the TMDB bearer token there.
 */
internal fun loggingInterceptor(
    isDebug: Boolean = BuildConfig.DEBUG,
    logger: HttpLoggingInterceptor.Logger = HttpLoggingInterceptor.Logger.DEFAULT,
): HttpLoggingInterceptor = HttpLoggingInterceptor(logger).apply {
    level = if (isDebug) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    redactHeader("Authorization")
}
