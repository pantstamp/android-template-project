package com.pantelisstampoulis.androidtemplateproject.network

import com.pantelisstampoulis.androidtemplateproject.network.model.MovieApiModel

internal class RetrofitNetworkDataSource(private val networkApi: RetrofitNetworkApi)
    : NetworkDataSource {

    override suspend fun getMovies(): List<MovieApiModel> {
        return networkApi.getMovies().results
    }
}