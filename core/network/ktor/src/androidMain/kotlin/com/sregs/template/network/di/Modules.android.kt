package com.sregs.template.network.di

import com.sregs.template.network.NetworkConstants
import com.sregs.template.network.utils.getUrlPattern
import com.sregs.template.network.utils.getUrlPins
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.CertificatePinner
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    single {
        OkHttp.create {
            config {
                val urlPins = NetworkConstants.UrlPins.getUrlPins()
                if (urlPins.isNotEmpty()) {
                    certificatePinner(
                        CertificatePinner.Builder()
                            .add(
                                pattern = NetworkConstants.Url.getUrlPattern(),
                                pins = urlPins,
                            )
                            .build(),
                    )
                }
            }
        }
    } bind HttpClientEngine::class
}
