package com.pantelisstampoulis.androidtemplateproject.network

import com.pantelisstampoulis.androidtemplateproject.network.model.MovieApiModel

internal class RetrofitNetworkDataSource(private val networkApi: RetrofitNetworkApi)
    : NetworkDataSource {

    override suspend fun getMovies(): NetworkResult<List<MovieApiModel>> {
        return networkApi.getMovies().let { result ->
            when (result) {
                is NetworkResult.Success -> NetworkResult.Success(result.data.results)
                is NetworkResult.Error -> result
                is NetworkResult.Exception -> result
            }
        }
    }
}