package com.sregs.template.network

import com.sregs.random.randomString
import com.sregs.template.network.di.testNetworkModule
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KtorNetworkDataSourceTest : KoinTest {

    private val dataSource: NetworkDataSource by inject()

    @BeforeTest
    fun setUp() {
        startKoin {
            modules(testNetworkModule)
        }
    }

    @AfterTest
    fun teardown() {
        stopKoin()
    }

    @Test
    fun getAccountsTest() = runTest {
        val response = dataSource.getAccounts()
        assertTrue { response.isNotEmpty() }
    }

    @Test
    fun getAccountDetailsTest() = runTest {
        val accountId = randomString()
        val response = dataSource.getAccountDetails(accountId = accountId)
        assertNotNull(response)
    }
}
