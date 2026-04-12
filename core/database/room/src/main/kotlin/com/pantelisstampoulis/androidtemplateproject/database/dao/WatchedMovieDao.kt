package com.pantelisstampoulis.androidtemplateproject.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pantelisstampoulis.androidtemplateproject.database.model.WatchedMovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchedMovieDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWatchedMovie(entity: WatchedMovieEntity): Long

    @Query("SELECT * FROM watched_movies ORDER BY ratedAt DESC")
    fun getWatchedMovieEntities(): Flow<List<WatchedMovieEntity>>

    @Query("SELECT * FROM watched_movies WHERE movieId = :movieId")
    fun getWatchedMovieEntity(movieId: Int): WatchedMovieEntity?
}
