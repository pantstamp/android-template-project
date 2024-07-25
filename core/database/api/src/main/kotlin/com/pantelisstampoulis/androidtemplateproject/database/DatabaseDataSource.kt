package com.pantelisstampoulis.androidtemplateproject.database

import com.pantelisstampoulis.androidtemplateproject.database.model.MovieDbModel


interface DatabaseDataSource {

    suspend fun deleteAndInsertMovies(
        movies: List<MovieDbModel>,
    )

    suspend fun getMovies(): List<MovieDbModel>
}
