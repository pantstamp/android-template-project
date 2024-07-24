package com.sregs.template.network.di

import com.sregs.template.network.NetworkDataSource
import com.sregs.template.network.NoopNetworkDataSource
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule: Module = module {

    single {
        NoopNetworkDataSource()
    } bind NetworkDataSource::class
}
