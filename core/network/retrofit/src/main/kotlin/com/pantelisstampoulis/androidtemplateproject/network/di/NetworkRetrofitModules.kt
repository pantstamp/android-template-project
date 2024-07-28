package com.pantelisstampoulis.androidtemplateproject.network.di

import com.pantelisstampoulis.androidtemplateproject.network.BASE_URL
import com.pantelisstampoulis.androidtemplateproject.network.NetworkDataSource
import com.pantelisstampoulis.androidtemplateproject.network.RetrofitNetworkApi
import com.pantelisstampoulis.androidtemplateproject.network.RetrofitNetworkDataSource
import com.pantelisstampoulis.androidtemplateproject.network.interceptor.HeaderInterceptor
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor


val networkModule: Module = module {

    // Provide OkHttpClient as a singleton
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor())
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // Provide Json as a singleton
    single {
        Json { ignoreUnknownKeys = true }
    }

    // Provide RetrofitNetworkApi as a singleton
    single {
        val okHttpClient: OkHttpClient = get()
        val networkJson: Json = get()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(networkJson.asConverterFactory("application/json; charset=UTF-8"
                .toMediaType()))
            .build()
            .create(RetrofitNetworkApi::class.java)
    }

    // Provide RetrofitNetworkDataSource as a singleton
    single {
        RetrofitNetworkDataSource(get())
    } bind NetworkDataSource::class
}