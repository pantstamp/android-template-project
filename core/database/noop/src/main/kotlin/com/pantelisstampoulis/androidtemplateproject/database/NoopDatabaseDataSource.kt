package com.pantelisstampoulis.androidtemplateproject.database

import com.pantelisstampoulis.androidtemplateproject.database.model.MovieDbModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class NoopDatabaseDataSource : DatabaseDataSource {

    override suspend fun insertMovies(movies: List<MovieDbModel>) {
        // empty implementation
    }

    override suspend fun getMovies(): Flow<List<MovieDbModel>> = flowOf(emptyList())

    override suspend fun getMovie(movieId: Int): MovieDbModel? = null
}
