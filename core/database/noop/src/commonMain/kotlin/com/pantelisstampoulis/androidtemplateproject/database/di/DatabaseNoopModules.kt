package com.pantelisstampoulis.androidtemplateproject.database.di

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule: Module = module {
    single {
        NoopDatabaseDataSource()
    } bind DatabaseDataSource::class
}
