package com.sregs.template.network.utils

import com.sregs.logging.Logger
import com.sregs.template.network.NetworkConstants
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal interface KtorHttpClientFactory {

    fun configure(): HttpClient
}

internal class KtorHttpClientFactoryImpl(
    private val log: Logger,
    private val json: Json,
    private val engine: HttpClientEngine,
    private val baseUrl: String = NetworkConstants.Url,
    private val timeout: Long = 30_000L,
    private val isNetworkLoggingEnabled: Boolean = NetworkConstants.IsNetworkLoggingEnabled,
    // TODO CS: find more elegant way, flavor or something
    private val authEnabled: Boolean = false,
) : KtorHttpClientFactory {

    override fun configure(): HttpClient = HttpClient(engine = engine) {
        expectSuccess = true

        install(plugin = ContentNegotiation) {
            json(json = json)
        }

        install(plugin = DefaultRequest) {
            url(urlString = baseUrl)
        }

        install(plugin = Logging) {
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    log.v { message }
                }
            }
            level = if (isNetworkLoggingEnabled) LogLevel.ALL else LogLevel.NONE
        }

        install(plugin = HttpTimeout) {
            connectTimeoutMillis = timeout
            requestTimeoutMillis = timeout
            socketTimeoutMillis = timeout
        }

        if (authEnabled) {
            install(plugin = Auth) {
                basic {
                    credentials {
                        // Provide your username and password here
                        // This could also be fetching from secure storage or other method
                        return@credentials BasicAuthCredentials(
                            username = "Advantage",
                            password = "mobileAssignment",
                        )
                    }
                }
            }
        }
    }
}
