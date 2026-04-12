package com.pantelisstampoulis.androidtemplateproject.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `watched_movies` (
                `movieId` INTEGER NOT NULL PRIMARY KEY,
                `title` TEXT NOT NULL,
                `posterUrl` TEXT,
                `overview` TEXT,
                `publicRating` REAL NOT NULL,
                `releaseDate` TEXT,
                `userRating` INTEGER NOT NULL,
                `ratedAt` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }
}
