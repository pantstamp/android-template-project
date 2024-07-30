package com.pantelisstampoulis.core.bridge_di

import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initKoin(
    modules: List<Module> = emptyList(),
): KoinApplication = startKoin {
    androidLogger()
    modules(
        coreModule,
        *modules.toTypedArray(),
    )
}


