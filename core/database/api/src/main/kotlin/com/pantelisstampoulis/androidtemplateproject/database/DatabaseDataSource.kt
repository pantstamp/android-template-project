package com.pantelisstampoulis.androidtemplateproject.database

import com.pantelisstampoulis.androidtemplateproject.database.model.MovieDbModel
import kotlinx.coroutines.flow.Flow

interface DatabaseDataSource {

    suspend fun insertMovies(
        movies: List<MovieDbModel>,
    )

    suspend fun getMovies(): Flow<List<MovieDbModel>>

    suspend fun getMovie(movieId: Int): MovieDbModel?
}
