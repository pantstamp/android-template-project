package com.pantelisstampoulis.androidtemplateproject.data.repository

import com.pantelisstampoulis.androidtemplateproject.database.DatabaseDataSource
import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import com.pantelisstampoulis.androidtemplateproject.network.NetworkDataSource
import com.pantelisstampoulis.androidtemplateproject.data.mapper.Mappers
import com.pantelisstampoulis.androidtemplateproject.database.model.MovieDbModel
import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.model.error.ErrorModel
import com.pantelisstampoulis.androidtemplateproject.network.NetworkResult
import com.pantelisstampoulis.androidtemplateproject.network.isSuccess
import com.pantelisstampoulis.androidtemplateproject.network.request.RateMovieRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

internal class MoviesRepositoryImpl(
    private val networkDataSource: NetworkDataSource,
    private val databaseDataSource: DatabaseDataSource,
    private val mappers: Mappers,
) : MoviesRepository {

    override fun getMovie(movieId: Int): Flow<ResultState<Movie>> = flow {
        // fetch movie from database
        val movieDbModel = databaseDataSource.getMovie(movieId)

        movieDbModel?.let {
            val movie = mappers.movieDomainMapper.fromDbToDomain(movieDbModel)
            emit(ResultState.Success(movie))
        } ?: run {
            emit(ResultState.Error(ErrorModel.NotFound()))
        }
    }

    override fun getMovies(ignoreCache: Boolean): Flow<ResultState<List<Movie>>> = flow {
        val movies = databaseDataSource.getMovies().firstOrNull() // Collect the first emission or null
        if (movies.isNullOrEmpty()) {
            // No movies in the database, fetch from network
            fetchMoviesFromNetwork()
        } else {
            // Movies are present in the database
            if (!ignoreCache) {
                emitMoviesFromDb(movies)
            } else {
                fetchMoviesFromNetwork()
            }
        }
    }

    override fun rateMovie(movieId: Int, rating: Float): Flow<ResultState<Unit>> = flow {
        val networkResult = networkDataSource.rateMovie(movieId, RateMovieRequest(rating))
        if (networkResult.isSuccess()) {
            emit(ResultState.Success(Unit))
        } else {
            val errorModel = mappers.errorDomainMapper.mapNetworkResultToErrorModel(networkResult)
            errorModel?.let { emit(ResultState.Error(it)) }
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
