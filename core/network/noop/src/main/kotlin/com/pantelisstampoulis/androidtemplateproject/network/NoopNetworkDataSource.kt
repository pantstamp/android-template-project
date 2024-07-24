package com.pantelisstampoulis.androidtemplateproject.network

internal class NoopNetworkDataSource : NetworkDataSource {

    override suspend fun getMovies(): List<MovieApiModel> = emptyList()
}
