package com.pantelisstampoulis.androidtemplateproject.network

import com.pantelisstampoulis.androidtemplateproject.network.model.MovieApiModel
import com.pantelisstampoulis.androidtemplateproject.network.request.RateMovieRequest
import com.pantelisstampoulis.androidtemplateproject.network.response.ApiDataResponse
import com.pantelisstampoulis.androidtemplateproject.network.response.ApiResultResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

const val BASE_URL = "https://api.themoviedb.org/3/"

const val IMAGE_URL = "https://image.tmdb.org/t/p/w500"

interface RetrofitNetworkApi {

    @GET(value = "discover/movie")
    suspend fun getMovies(): NetworkResult<ApiDataResponse<List<MovieApiModel>>>

    @POST(value = "movie/{movie_id}/rating")
    suspend fun rateMovie(@Path("movie_id") movieId: Int, @Body request: RateMovieRequest): NetworkResult<ApiResultResponse>
}
