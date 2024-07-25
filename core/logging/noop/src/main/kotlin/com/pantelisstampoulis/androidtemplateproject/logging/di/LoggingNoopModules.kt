package com.pantelisstampoulis.androidtemplateproject.logging.di

import com.pantelisstampoulis.androidtemplateproject.logging.Logger
import com.pantelisstampoulis.androidtemplateproject.logging.NoopLogger
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

val loggingModule: Module = module {

    factory { (tag: String?) ->
        NoopLogger(tag = tag.orEmpty())
    } bind Logger::class
}
