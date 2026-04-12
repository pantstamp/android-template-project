package com.pantelisstampoulis.androidtemplateproject.database

import com.pantelisstampoulis.androidtemplateproject.database.mapper.Mappers
import com.pantelisstampoulis.androidtemplateproject.database.model.MovieDbModel
import com.pantelisstampoulis.androidtemplateproject.database.model.WatchedMovieDbModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomDataSource(
    private val db: AppDatabase,
    private val mappers: Mappers,
) : DatabaseDataSource {

    override suspend fun insertMovies(movies: List<MovieDbModel>) {
        db.movieDao().insertMovies(movies.map { mappers.movieDbMapper.toDb(it) })
    }

    override suspend fun getMovies(): Flow<List<MovieDbModel>> = db.movieDao().getMovieEntities().map { movieEntityList ->
        movieEntityList.map { movieEntity ->
            mappers.movieDbMapper.mapFromDb(movieEntity)
        }
    }

    override suspend fun getMovie(movieId: Int): MovieDbModel? = db.movieDao().getMovieEntity(movieId)?.let { movieEntity ->
        mappers.movieDbMapper.mapFromDb(movieEntity)
    }

    override suspend fun insertWatchedMovie(movie: WatchedMovieDbModel) {
        db.watchedMovieDao().insertWatchedMovie(mappers.watchedMovieDbMapper.toDb(movie))
    }

    override suspend fun getWatchedMovies(): Flow<List<WatchedMovieDbModel>> =
        db.watchedMovieDao().getWatchedMovieEntities().map { entities ->
            entities.map { mappers.watchedMovieDbMapper.mapFromDb(it) }
        }

    override suspend fun getWatchedMovie(movieId: Int): WatchedMovieDbModel? =
        db.watchedMovieDao().getWatchedMovieEntity(movieId)?.let {
            mappers.watchedMovieDbMapper.mapFromDb(it)
        }
}
