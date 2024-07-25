package com.pantelisstampoulis.androidtemplateproject.data.repository

import com.pantelisstampoulis.androidtemplateproject.database.DatabaseDataSource
import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import com.pantelisstampoulis.androidtemplateproject.network.NetworkDataSource
import com.pantelisstampoulis.androidtemplateproject.data.mapper.Mappers
import kotlinx.coroutines.flow.Flow

internal class MoviesRepositoryImpl(
    private val networkDataSource: NetworkDataSource,
    private val databaseDataSource: DatabaseDataSource,
    private val mappers: Mappers,
) : MoviesRepository {

    override fun getMovies(ignoreCache: Boolean): Flow<List<Movie>> {
        TODO("Not yet implemented")
    }
}
