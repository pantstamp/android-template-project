package com.pantelisstampoulis.androidtemplateproject.database

import com.pantelisstampoulis.androidtemplateproject.database.model.MovieDbModel
import com.pantelisstampoulis.androidtemplateproject.database.model.WatchedMovieDbModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class NoopDatabaseDataSource : DatabaseDataSource {

    override suspend fun insertMovies(movies: List<MovieDbModel>) {
        // empty implementation
    }

    override suspend fun getMovies(): Flow<List<MovieDbModel>> = flowOf(emptyList())

    override suspend fun getMovie(movieId: Int): MovieDbModel? = null

    override suspend fun insertWatchedMovie(movie: WatchedMovieDbModel) { }

    override fun getWatchedMovies(): Flow<List<WatchedMovieDbModel>> = flowOf(emptyList())

    override suspend fun getWatchedMovie(movieId: Int): WatchedMovieDbModel? = null
}
