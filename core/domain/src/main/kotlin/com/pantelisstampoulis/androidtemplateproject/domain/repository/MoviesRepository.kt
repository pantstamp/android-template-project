package com.pantelisstampoulis.androidtemplateproject.domain.repository

import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import kotlinx.coroutines.flow.Flow

interface MoviesRepository {

    fun getMovies(ignoreCache: Boolean = false): Flow<List<Movie>>
}
