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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEmpty

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
        databaseDataSource.getMovies()
            .onEmpty {
                // The flow is empty, fetch movies from the network
                fetchMoviesFromNetwork()
            }
            .collect { movieDbModelList ->
                // If the flow is not empty and cache is not ignored, emit movies from the database
                if (!ignoreCache) {
                    emitMoviesFromDb(movieDbModelList)
                } else {
                    // If cache is ignored, still fetch from network
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
