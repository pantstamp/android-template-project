package com.pantelisstampoulis.androidtemplateproject.network

import com.pantelisstampoulis.androidtemplateproject.network.model.MovieApiModel

interface NetworkDataSource {

    suspend fun getMovies(): List<MovieApiModel>

}
