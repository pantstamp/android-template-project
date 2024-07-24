package com.sregs.template.network.di

import com.sregs.template.network.NetworkDataSource
import com.sregs.template.network.NoopNetworkDataSource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class NetworkNoopModulesTest : KoinTest {

    @BeforeTest
    fun setUp() {
        startKoin {
            modules(networkModule)
        }
    }

    @AfterTest
    fun teardown() {
        stopKoin()
    }

    @Test
    fun databaseModuleTest() {
        val dataSource = get<NetworkDataSource>()
        assertTrue { dataSource is NoopNetworkDataSource }
    }
}
