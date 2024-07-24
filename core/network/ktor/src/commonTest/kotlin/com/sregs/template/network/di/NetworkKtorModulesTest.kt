package com.sregs.template.network.di

import com.sregs.logging.di.loggingModule
import com.sregs.serialization.di.serializationModule
import com.sregs.template.network.KtorNetworkDataSource
import com.sregs.template.network.NetworkDataSource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class NetworkKtorModulesTest : KoinTest {

    @BeforeTest
    fun setUp() {
        startKoin {
            modules(networkModule, loggingModule, serializationModule)
        }
    }

    @AfterTest
    fun teardown() {
        stopKoin()
    }

    @Test
    fun networkModuleTest() {
        val dataSource = get<NetworkDataSource>()
        assertTrue { dataSource is KtorNetworkDataSource }
    }
}
