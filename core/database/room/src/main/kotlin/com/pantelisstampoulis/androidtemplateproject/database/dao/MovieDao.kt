package com.pantelisstampoulis.androidtemplateproject.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pantelisstampoulis.androidtemplateproject.database.model.MovieEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for [MovieEntity] access
 */
@Dao
interface MovieDao {

    /**
     * Inserts [movieEntities] into the db if they don't exist, and replaces those that do
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movieEntities: List<MovieEntity>): List<Long>

    @Query(
        value = """
        SELECT * FROM movies
        WHERE id = :movieId
    """,
    )
    fun getMovieEntity(movieId: Int): MovieEntity?

    @Query(value = "SELECT * FROM movies")
    fun getMovieEntities(): Flow<List<MovieEntity>>
}
