package com.pantelisstampoulis.androidtemplateproject.dispatcher.di

import com.pantelisstampoulis.androidtemplateproject.dispatcher.CoroutinesDispatchers
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

val dispatcherModule: Module = module {
    single<CoroutineContext>(named(CoroutinesDispatchers.Default)) {
        Dispatchers.Default
    }

    single<CoroutineContext>(named(CoroutinesDispatchers.MainImmediate)) {
        Dispatchers.Main.immediate
    }

    single<CoroutineContext>(named(CoroutinesDispatchers.Main)) {
        Dispatchers.Main
    }

    single<CoroutineContext>(named(CoroutinesDispatchers.IO)) {
        Dispatchers.IO
    }
}
