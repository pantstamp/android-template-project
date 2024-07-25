package com.pantelisstampoulis.androidtemplateproject.dispatcher.di

import com.pantelisstampoulis.androidtemplateproject.dispatcher.CoroutinesDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
val dispatcherModule: Module = module {
    single<CoroutineContext>(named(CoroutinesDispatchers.Default)) {
        UnconfinedTestDispatcher()
    }

    single<CoroutineContext>(named(CoroutinesDispatchers.Main)) {
        UnconfinedTestDispatcher()
    }

    single<CoroutineContext>(named(CoroutinesDispatchers.MainImmediate)) {
        UnconfinedTestDispatcher()
    }

    single<CoroutineContext>(named(CoroutinesDispatchers.IO)) {
        UnconfinedTestDispatcher()
    }
}
