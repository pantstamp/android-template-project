package com.pantelisstampoulis.androidtemplateproject.domain.repository

import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import com.pantelisstampoulis.androidtemplateproject.model.movies.WatchedMovie
import kotlinx.coroutines.flow.Flow

interface MoviesRepository {

    fun getMovie(movieId: Int): Flow<ResultState<Movie>>

    fun getMovies(ignoreCache: Boolean = false): Flow<ResultState<List<Movie>>>

    fun rateMovie(movieId: Int, rating: Float): Flow<ResultState<Unit>>

    fun saveWatchedMovie(
        movieId: Int,
        title: String,
        posterUrl: String?,
        overview: String?,
        publicRating: Double,
        releaseDate: String?,
        userRating: Int,
    ): Flow<ResultState<Unit>>

    fun getWatchedMovies(): Flow<ResultState<List<WatchedMovie>>>

    fun getWatchedMovie(movieId: Int): Flow<ResultState<WatchedMovie>>
}
