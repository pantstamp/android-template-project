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

// TMDB returns null backdrop_path/poster_path, empty genre_ids and blank release_date for some
// movies. SQLite cannot drop a NOT NULL constraint in place, so the movies table is rebuilt
// with those columns nullable.
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `movies_new` (
                `id` INTEGER NOT NULL,
                `adult` INTEGER NOT NULL,
                `backdropPath` TEXT,
                `genreId` INTEGER,
                `originalLanguage` TEXT NOT NULL,
                `originalTitle` TEXT NOT NULL,
                `overview` TEXT NOT NULL,
                `popularity` REAL NOT NULL,
                `posterPath` TEXT,
                `releaseDate` TEXT,
                `title` TEXT NOT NULL,
                `video` INTEGER NOT NULL,
                `voteAverage` REAL NOT NULL,
                `voteCount` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL("INSERT INTO `movies_new` SELECT * FROM `movies`")
        db.execSQL("DROP TABLE `movies`")
        db.execSQL("ALTER TABLE `movies_new` RENAME TO `movies`")
    }
}
