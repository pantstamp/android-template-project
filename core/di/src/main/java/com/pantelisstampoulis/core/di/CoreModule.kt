package com.pantelisstampoulis.core.di

import com.pantelisstampoulis.androidtemplateproject.data.di.dataModule
import com.pantelisstampoulis.androidtemplateproject.dispatcher.di.dispatcherModule
import com.pantelisstampoulis.androidtemplateproject.domain.di.domainModule
import com.pantelisstampoulis.androidtemplateproject.logging.di.loggingModule
import org.koin.core.module.Module
import org.koin.dsl.module

val coreModule: Module = module {
    includes(
        // core
        domainModule,
        dataModule,
        dispatcherModule,
        loggingModule,
    )
}
