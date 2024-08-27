package com.pantelisstampoulis.androidtemplateproject.network.di

import android.util.Log
import com.pantelisstampoulis.androidtemplateproject.network.NetworkDataSource
import com.pantelisstampoulis.androidtemplateproject.network.RetrofitNetworkApi
import com.pantelisstampoulis.androidtemplateproject.network.RetrofitNetworkDataSource
import com.pantelisstampoulis.androidtemplateproject.network.adapter.NetworkResultCallAdapterFactory
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
import okhttp3.mockwebserver.MockWebServer
import org.koin.core.qualifier.named


val testNetworkModule: Module = module {

    single {
        MockWebServer()
    }

    single<String>(qualifier = named("baseUrl")) {
        get<MockWebServer>().url("/").toString() // Provide the base URL from MockWebServer
    }

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

        val baseUrl = get<String>(named("baseUrl"))
        //Log.d("myTest","baseUrl = $baseUrl")

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(networkJson.asConverterFactory("application/json; charset=UTF-8"
                .toMediaType()))
            .addCallAdapterFactory(NetworkResultCallAdapterFactory.create())
            .build()
            .create(RetrofitNetworkApi::class.java)
    }

    // Provide RetrofitNetworkDataSource as a singleton
    single {
        RetrofitNetworkDataSource(get())
    } bind NetworkDataSource::class
}