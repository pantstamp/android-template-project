package com.pantelisstampoulis.androidtemplateproject.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pantelisstampoulis.androidtemplateproject.database.dao.MovieDao
import com.pantelisstampoulis.androidtemplateproject.database.dao.WatchedMovieDao
import com.pantelisstampoulis.androidtemplateproject.database.model.MovieEntity
import com.pantelisstampoulis.androidtemplateproject.database.model.WatchedMovieEntity

@Database(
    entities = [
        MovieEntity::class,
        WatchedMovieEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun watchedMovieDao(): WatchedMovieDao
}
