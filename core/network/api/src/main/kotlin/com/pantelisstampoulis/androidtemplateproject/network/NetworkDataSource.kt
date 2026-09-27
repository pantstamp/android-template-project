package com.pantelisstampoulis.androidtemplateproject.network

import com.pantelisstampoulis.androidtemplateproject.network.model.MovieApiModel
import com.pantelisstampoulis.androidtemplateproject.network.model.MovieDetailsApiModel
import com.pantelisstampoulis.androidtemplateproject.network.request.RateMovieRequest
import com.pantelisstampoulis.androidtemplateproject.network.response.ApiResultResponse

interface NetworkDataSource {

    suspend fun getMovies(): NetworkResult<List<MovieApiModel>>

    /** First page of title matches, adult content excluded. */
    suspend fun searchMovies(query: String): NetworkResult<List<MovieApiModel>>

    suspend fun getMovie(movieId: Int): NetworkResult<MovieDetailsApiModel>

    suspend fun rateMovie(movieId: Int, request: RateMovieRequest): NetworkResult<ApiResultResponse>
}
