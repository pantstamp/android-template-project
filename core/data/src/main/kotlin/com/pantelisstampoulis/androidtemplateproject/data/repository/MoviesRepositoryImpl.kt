package com.pantelisstampoulis.androidtemplateproject.data.repository

import com.pantelisstampoulis.androidtemplateproject.database.DatabaseDataSource
import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import com.pantelisstampoulis.androidtemplateproject.network.NetworkDataSource
import com.pantelisstampoulis.androidtemplateproject.data.mapper.Mappers
import com.pantelisstampoulis.androidtemplateproject.database.model.MovieDbModel
import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.network.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

internal class MoviesRepositoryImpl(
    private val networkDataSource: NetworkDataSource,
    private val databaseDataSource: DatabaseDataSource,
    private val mappers: Mappers,
) : MoviesRepository {

    override fun getMovies(ignoreCache: Boolean): Flow<ResultState<List<Movie>>> = flow {
        // fetch movies from database
        val movieDbModelList = databaseDataSource.getMovies()

        if (movieDbModelList.isNotEmpty() && !ignoreCache) {
            emitMoviesFromDb(movieDbModelList)
        } else {
            // fetch movies from network
            fetchMoviesFromNetwork()
        }
    }

    private suspend fun FlowCollector<ResultState<List<Movie>>>.emitMoviesFromDb(movieDbModelList: List<MovieDbModel>) {
        val movieDomainList = movieDbModelList.map(mappers.movieDomainMapper::fromDbToDomain)
        emit(ResultState.Success(movieDomainList))
    }

    private suspend fun FlowCollector<ResultState<List<Movie>>>.fetchMoviesFromNetwork() {
        when (val moviesNetworkResult = networkDataSource.getMovies()) {
            is NetworkResult.Success -> {
                val movieList = moviesNetworkResult.data.map(mappers.movieDataMapper::fromApiToDb)
                // save movies to database
                databaseDataSource.deleteAndInsertMovies(movieList)
                emitMoviesFromDb(movieList)
            }
            is NetworkResult.Error, is NetworkResult.Exception -> {
                val errorModel = mappers.errorDomainMapper.mapNetworkResultToErrorModel(moviesNetworkResult)
                errorModel?.let { emit(ResultState.Error(it)) }
            }
        }
    }
}
