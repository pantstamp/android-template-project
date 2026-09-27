package com.pantelisstampoulis.androidtemplateproject.network

import com.pantelisstampoulis.androidtemplateproject.network.model.MovieApiModel
import com.pantelisstampoulis.androidtemplateproject.network.model.MovieDetailsApiModel
import com.pantelisstampoulis.androidtemplateproject.network.request.RateMovieRequest
import com.pantelisstampoulis.androidtemplateproject.network.response.ApiResultResponse

internal class RetrofitNetworkDataSource(private val networkApi: RetrofitNetworkApi) : NetworkDataSource {

    override suspend fun getMovies(): NetworkResult<List<MovieApiModel>> = networkApi.getMovies().let { result ->
        when (result) {
            is NetworkResult.Success -> NetworkResult.Success(result.data.results)
            is NetworkResult.Error -> result
            is NetworkResult.Exception -> result
        }
    }

    override suspend fun searchMovies(query: String): NetworkResult<List<MovieApiModel>> =
        // include_adult is passed explicitly rather than relying on TMDB's default.
        networkApi.searchMovies(query = query, includeAdult = false).let { result ->
            when (result) {
                is NetworkResult.Success -> NetworkResult.Success(result.data.results)
                is NetworkResult.Error -> result
                is NetworkResult.Exception -> result
            }
        }

    override suspend fun getMovie(movieId: Int): NetworkResult<MovieDetailsApiModel> = networkApi.getMovie(movieId)

    override suspend fun rateMovie(movieId: Int, request: RateMovieRequest): NetworkResult<ApiResultResponse> =
        networkApi.rateMovie(movieId, request)
}
