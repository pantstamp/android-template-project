package com.pantelisstampoulis.androidtemplateproject.database.di

import androidx.room.Room
import com.pantelisstampoulis.androidtemplateproject.database.AppDatabase
import com.pantelisstampoulis.androidtemplateproject.database.DatabaseDataSource
import com.pantelisstampoulis.androidtemplateproject.database.RoomDataSource
import com.pantelisstampoulis.androidtemplateproject.database.mapper.Mappers
import com.pantelisstampoulis.androidtemplateproject.database.mapper.MovieDbMapper
import com.pantelisstampoulis.androidtemplateproject.database.mapper.WatchedMovieDbMapper
import com.pantelisstampoulis.androidtemplateproject.database.migration.MIGRATION_1_2
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal val mappersModule = module {
    factory {
        Mappers(
            movieDbMapper = MovieDbMapper(),
            watchedMovieDbMapper = WatchedMovieDbMapper(),
        )
    }
}

val databaseModule: Module = module {
    includes(mappersModule)

    single {
        Room.databaseBuilder(
            get(),
            AppDatabase::class.java,
            "app-database",
        ).addMigrations(MIGRATION_1_2).build()
    }

    single {
        RoomDataSource(get(), get())
    } bind DatabaseDataSource::class
}
