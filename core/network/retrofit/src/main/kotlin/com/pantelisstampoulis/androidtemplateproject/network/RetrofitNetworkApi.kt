package com.pantelisstampoulis.androidtemplateproject.network

import com.pantelisstampoulis.androidtemplateproject.network.model.MovieApiModel
import retrofit2.http.GET

interface RetrofitNetworkApi {
    @GET(value = "discover/movie")
    suspend fun getMovies(): NetworkResponse<List<MovieApiModel>>
}