package com.pantelisstampoulis.androidtemplateproject.network

import com.pantelisstampoulis.androidtemplateproject.network.model.MovieApiModel
import retrofit2.http.GET

const val BASE_URL = "https://api.themoviedb.org/3/"

interface RetrofitNetworkApi {
    @GET(value = "discover/movie")
    suspend fun getMovies(): NetworkResult<ApiResponse<List<MovieApiModel>>>
}