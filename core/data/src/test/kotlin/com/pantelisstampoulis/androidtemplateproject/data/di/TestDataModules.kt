package com.pantelisstampoulis.androidtemplateproject.data.di

import com.pantelisstampoulis.androidtemplateproject.data.repository.MoviesRepositoryImpl
import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import org.koin.dsl.bind
import org.koin.dsl.module

val testDataModule = module {
    includes(
        mappersModule
    )

    single {
        MoviesRepositoryImpl(
            databaseDataSource = get(),
            networkDataSource = get(),
            mappers = get(),
        )
    } bind MoviesRepository::class
}
