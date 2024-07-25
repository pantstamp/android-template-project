package com.pantelisstampoulis.androidtemplateproject.database

import com.pantelisstampoulis.androidtemplateproject.database.mapper.Mappers
import com.pantelisstampoulis.androidtemplateproject.database.model.MovieDbModel

internal class RoomDataSource(
    private val db: AppDatabase,
    private val mappers: Mappers,
) : DatabaseDataSource {

    override suspend fun deleteAndInsertMovies(movies: List<MovieDbModel>) {
        db.movieDao().insertAndReplaceMovies(movies.map { mappers.movieDbMapper.toDb(it) })
    }

    override suspend fun getMovies(): List<MovieDbModel> {
       return db.movieDao().getMovieEntities().map { mappers.movieDbMapper.mapFromDb(it) }
    }
}
