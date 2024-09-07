package com.pantelisstampoulis.androidtemplateproject.network.di

import com.pantelisstampoulis.androidtemplateproject.network.NetworkDataSource
import com.pantelisstampoulis.androidtemplateproject.network.NoopNetworkDataSource
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule: Module = module {

    single {
        NoopNetworkDataSource()
    } bind NetworkDataSource::class
}
