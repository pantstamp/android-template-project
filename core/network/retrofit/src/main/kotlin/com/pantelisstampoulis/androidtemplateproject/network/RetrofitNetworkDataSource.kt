package com.pantelisstampoulis.androidtemplateproject.network

import com.pantelisstampoulis.androidtemplateproject.network.model.MovieApiModel

const val BASE_URL = "https://api.themoviedb.org/3/"

internal class RetrofitNetworkDataSource(private val networkApi: RetrofitNetworkApi)
    : NetworkDataSource {

    override suspend fun getMovies(): List<MovieApiModel> {
        return networkApi.getMovies().data
    }
}