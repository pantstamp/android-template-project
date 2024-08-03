package com.pantelisstampoulis.androidtemplateproject.domain.repository

import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import kotlinx.coroutines.flow.Flow

interface MoviesRepository {

    fun getMovies(ignoreCache: Boolean = false): Flow<ResultState<List<Movie>>>

    fun rateMovie(movieId: Int, rating: Float): Flow<ResultState<Unit>>
}
