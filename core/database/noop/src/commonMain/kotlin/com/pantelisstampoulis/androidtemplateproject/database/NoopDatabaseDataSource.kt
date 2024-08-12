package com.pantelisstampoulis.androidtemplateproject.database

internal class NoopDatabaseDataSource : DatabaseDataSource {

    override suspend fun deleteAndInsertMovies(accounts: List<MovieDbModel>) {
        /* empty implementation */
    }

    override suspend fun getMovies(): Flow<List<MovieDbModel>> = flowOf(emptyList())

    override suspend fun getMovie(movieId: Int): MovieDbModel? = null
}
