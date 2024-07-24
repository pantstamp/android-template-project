package com.sregs.template.network.utils

import com.sregs.template.network.TestConstants
import com.sregs.template.network.config.Paths
import com.sregs.template.test.doubles.network.NetworkTestDoubleFactory
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface MockEngineFactory {

    fun configure(): HttpClientEngine
}

class MockEngineFactoryImpl(
    private val json: Json,
    private val doubleFactory: NetworkTestDoubleFactory,
    private val baseUrl: String = TestConstants.BaseUrl,
) : MockEngineFactory {

    override fun configure(): HttpClientEngine = MockEngine.create {
        addHandler { request ->
            val fullUrl = request.url.fullUrl
            when {
                fullUrl == "$baseUrl/${Paths.Accounts}" -> mockResponse(
                    value = listOf(doubleFactory.provideAccountApi()),
                )

                fullUrl.startsWith(prefix = "$baseUrl/${Paths.AccountDetails}") -> mockResponse(
                    value = doubleFactory.provideAccountDetailsApi(),
                )

                else -> error("Unhandled url: $fullUrl")
            }
        }
    }

    private inline fun <reified T> MockRequestHandleScope.mockResponse(
        value: T,
        status: HttpStatusCode = HttpStatusCode.OK,
        headers: Headers = headersOf(
            "Content-Type" to listOf(ContentType.Application.Json.toString()),
        ),
    ): HttpResponseData = respond(
        content = json.encodeToString(value = value),
        status = status,
        headers = headers,
    )

    private val Url.fullUrl: String get() = "${protocol.name}://$host$encodedPath"
}
