package com.pantelisstampoulis.core.di

import com.pantelisstampoulis.androidtemplateproject.data.di.dataModule
import com.pantelisstampoulis.androidtemplateproject.database.di.databaseModule
import com.pantelisstampoulis.androidtemplateproject.dispatcher.di.dispatcherModule
import com.pantelisstampoulis.androidtemplateproject.domain.di.domainModule
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.di.featureMovieCatalogModule
import com.pantelisstampoulis.androidtemplateproject.logging.di.loggingModule
import com.pantelisstampoulis.androidtemplateproject.network.di.networkModule
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {
    includes(
        // core
        domainModule,
        dataModule,
        databaseModule,
        networkModule,
        dispatcherModule,
        loggingModule,

        // feature
        featureMovieCatalogModule
    )
}
