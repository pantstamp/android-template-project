package com.sregs.template.network.di

import com.sregs.logging.di.loggingModule
import com.sregs.serialization.di.serializationModule
import com.sregs.template.network.KtorNetworkDataSource
import com.sregs.template.network.NetworkDataSource
import com.sregs.template.network.TestConstants
import com.sregs.template.network.utils.KtorHttpClientFactory
import com.sregs.template.network.utils.KtorHttpClientFactoryImpl
import com.sregs.template.network.utils.MockEngineFactory
import com.sregs.template.network.utils.MockEngineFactoryImpl
import com.sregs.template.test.doubles.network.di.testDoublesNetworkModule
import com.sregs.utils.koin.getWith
import org.koin.dsl.bind
import org.koin.dsl.module

val testNetworkModule = module {
    includes(loggingModule, serializationModule, testDoublesNetworkModule)

    single {
        KtorNetworkDataSource(httpClient = get<KtorHttpClientFactory>().configure())
    } bind NetworkDataSource::class

    single {
        KtorHttpClientFactoryImpl(
            log = getWith(null),
            json = get(),
            engine = get<MockEngineFactory>().configure(),
            baseUrl = TestConstants.BaseUrl,
            timeout = 5_000L,
        )
    } bind KtorHttpClientFactory::class

    single {
        MockEngineFactoryImpl(
            json = get(),
            doubleFactory = get(),
        )
    } bind MockEngineFactory::class
}
