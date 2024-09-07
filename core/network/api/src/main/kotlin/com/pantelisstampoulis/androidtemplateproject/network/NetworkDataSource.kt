package com.pantelisstampoulis.androidtemplateproject.network

import com.pantelisstampoulis.androidtemplateproject.network.model.MovieApiModel
import com.pantelisstampoulis.androidtemplateproject.network.request.RateMovieRequest
import com.pantelisstampoulis.androidtemplateproject.network.response.ApiResultResponse

interface NetworkDataSource {

    suspend fun getMovies(): NetworkResult<List<MovieApiModel>>

    suspend fun rateMovie(movieId: Int, request: RateMovieRequest): NetworkResult<ApiResultResponse>
}
