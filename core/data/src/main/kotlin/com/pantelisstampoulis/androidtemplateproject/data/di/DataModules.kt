package com.pantelisstampoulis.androidtemplateproject.data.di

import com.pantelisstampoulis.androidtemplateproject.data.mapper.MovieDataMapper
import com.pantelisstampoulis.androidtemplateproject.data.mapper.MovieDomainMapper
import com.pantelisstampoulis.androidtemplateproject.data.mapper.Mappers
import com.pantelisstampoulis.androidtemplateproject.data.repository.MoviesRepositoryImpl
import com.pantelisstampoulis.androidtemplateproject.database.di.databaseModule
import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import com.pantelisstampoulis.androidtemplateproject.network.di.networkModule
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal val mappersModule = module {
    single {
        Mappers(
            movieDataMapper = MovieDataMapper(),
            movieDomainMapper = MovieDomainMapper(),
        )
    }
}

val dataModule: Module = module {
    includes(
        mappersModule,
        databaseModule,
        networkModule,
    )

    single {
        MoviesRepositoryImpl(
            networkDataSource = get(),
            databaseDataSource = get(),
            mappers = get(),
        )
    } bind MoviesRepository::class
}
