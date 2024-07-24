package com.sregs.template.network.di

import com.sregs.template.network.KtorNetworkDataSource
import com.sregs.template.network.NetworkDataSource
import com.sregs.template.network.utils.KtorHttpClientFactory
import com.sregs.template.network.utils.KtorHttpClientFactoryImpl
import com.sregs.utils.koin.getWith
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule: Module = module {
    includes(platformModule)

    single {
        KtorNetworkDataSource(httpClient = get<KtorHttpClientFactory>().configure())
    } bind NetworkDataSource::class

    single {
        KtorHttpClientFactoryImpl(
            log = getWith("HttpClient"),
            json = get(),
            engine = get(),
            authEnabled = true,
        )
    } bind KtorHttpClientFactory::class
}

internal expect val platformModule: Module
