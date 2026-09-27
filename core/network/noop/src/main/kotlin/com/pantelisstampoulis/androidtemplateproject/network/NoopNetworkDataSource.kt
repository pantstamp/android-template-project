package com.pantelisstampoulis.androidtemplateproject.network

import com.pantelisstampoulis.androidtemplateproject.network.model.MovieApiModel
import com.pantelisstampoulis.androidtemplateproject.network.model.MovieDetailsApiModel
import com.pantelisstampoulis.androidtemplateproject.network.request.RateMovieRequest
import com.pantelisstampoulis.androidtemplateproject.network.response.ApiResultResponse

internal class NoopNetworkDataSource : NetworkDataSource {
    override suspend fun getMovies(): NetworkResult<List<MovieApiModel>> = NetworkResult.Success(emptyList())

    override suspend fun searchMovies(query: String): NetworkResult<List<MovieApiModel>> =
        NetworkResult.Success(emptyList())

    override suspend fun getMovie(movieId: Int): NetworkResult<MovieDetailsApiModel> =
        NetworkResult.Error(code = 404, message = null)

    override suspend fun rateMovie(movieId: Int, request: RateMovieRequest): NetworkResult<ApiResultResponse> =
        NetworkResult.Success(ApiResultResponse(success = true, statusCode = 200))
}
