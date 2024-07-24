package com.sregs.template.network.di

import com.sregs.template.network.NetworkConstants
import com.sregs.template.network.utils.getUrlPattern
import com.sregs.template.network.utils.getUrlPins
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.certificates.CertificatePinner
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    single {
        Darwin.create {
            val urlPins = NetworkConstants.UrlPins.getUrlPins()
            if (urlPins.isNotEmpty()) {
                val builder = CertificatePinner.Builder()
                    .add(
                        pattern = NetworkConstants.Url.getUrlPattern(),
                        pins = urlPins,
                    )
                handleChallenge(builder.build())
            }
        }
    } bind HttpClientEngine::class
}
