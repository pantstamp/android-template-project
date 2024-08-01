package com.pantelisstampoulis.androidtemplateproject.data.repository

import com.pantelisstampoulis.androidtemplateproject.database.DatabaseDataSource
import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import com.pantelisstampoulis.androidtemplateproject.network.NetworkDataSource
import com.pantelisstampoulis.androidtemplateproject.data.mapper.Mappers
import com.pantelisstampoulis.androidtemplateproject.database.model.MovieDbModel
import com.pantelisstampoulis.androidtemplateproject.network.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class MoviesRepositoryImpl(
    private val networkDataSource: NetworkDataSource,
    private val databaseDataSource: DatabaseDataSource,
    private val mappers: Mappers,
) : MoviesRepository {

    override fun getMovies(ignoreCache: Boolean): Flow<List<Movie>> = flow {
        val movieEntityList = databaseDataSource.getMovies()
        val movieList: List<MovieDbModel> = if (movieEntityList.isEmpty() || ignoreCache) {
            when (val movieApiModelList = networkDataSource.getMovies()) {
                is NetworkResult.Success -> {
                    val movieList = movieApiModelList.data.map { movie ->
                        mappers.movieDataMapper.fromApiToDb(apiModel = movie)
                    }
                    databaseDataSource.deleteAndInsertMovies(movies = movieList)
                    movieList
                }
                is NetworkResult.Error -> {
                    // todo handle error
                    emptyList()
                }
                is NetworkResult.Exception -> {
                    // todo handle error
                    emptyList()
                }
            }
        } else {
            movieEntityList
        }

        emit(
            movieList.map { movie ->
                mappers.movieDomainMapper.fromDbToDomain(
                    dbModel = movie,
                )
            }
        )
    }
}
