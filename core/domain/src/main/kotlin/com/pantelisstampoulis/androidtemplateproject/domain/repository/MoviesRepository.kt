package com.pantelisstampoulis.androidtemplateproject.domain.repository

import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import com.pantelisstampoulis.androidtemplateproject.model.movies.WatchedMovie
import kotlinx.coroutines.flow.Flow

interface MoviesRepository {

    /**
     * Emits the movie for [movieId]: from the database when it is there (a Discover movie),
     * otherwise fetched from the network. A fetched movie is never written to the database,
     * so opening it cannot add it to the Discover list.
     */
    fun getMovie(movieId: Int): Flow<ResultState<Movie>>

    /**
     * Searches TMDB by title. Network only: results are never written to the database, so they
     * can never appear in the Discover list. An empty result is `Success(emptyList())`.
     */
    fun searchMovies(query: String): Flow<ResultState<List<Movie>>>

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

    /**
     * Emits the watched movie for [movieId], or `Success(null)` when the movie has not been
     * rated. Absence is a normal outcome here, not an error.
     */
    fun getWatchedMovie(movieId: Int): Flow<ResultState<WatchedMovie?>>
}
