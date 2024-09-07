package com.pantelisstampoulis.androidtemplateproject.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pantelisstampoulis.androidtemplateproject.database.dao.MovieDao
import com.pantelisstampoulis.androidtemplateproject.database.model.MovieEntity

@Database(
    entities = [
        MovieEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}
