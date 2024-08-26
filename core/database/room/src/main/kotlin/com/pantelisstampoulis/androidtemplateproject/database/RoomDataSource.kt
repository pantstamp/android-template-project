package com.pantelisstampoulis.androidtemplateproject.database

import com.pantelisstampoulis.androidtemplateproject.database.mapper.Mappers
import com.pantelisstampoulis.androidtemplateproject.database.model.MovieDbModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomDataSource(
    private val db: AppDatabase,
    private val mappers: Mappers,
) : DatabaseDataSource {

    override suspend fun insertMovies(movies: List<MovieDbModel>) {
        db.movieDao().insertMovies(movies.map { mappers.movieDbMapper.toDb(it) })
    }

    override suspend fun getMovies(): Flow<List<MovieDbModel>> {
       return db.movieDao().getMovieEntities().map { movieEntityList ->
           movieEntityList.map {
               movieEntity -> mappers.movieDbMapper.mapFromDb(movieEntity)
           }
       }
    }

    override suspend fun getMovie(movieId: Int): MovieDbModel? {
        return db.movieDao().getMovieEntity(movieId)?.let { movieEntity ->
            mappers.movieDbMapper.mapFromDb(movieEntity)
        }
    }
}
