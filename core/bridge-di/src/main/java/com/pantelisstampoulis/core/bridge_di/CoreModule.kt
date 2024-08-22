package com.pantelisstampoulis.core.bridge_di

import com.pantelisstampoulis.androidtemplateproject.data.di.dataModule
import com.pantelisstampoulis.androidtemplateproject.dispatcher.di.dispatcherModule
import com.pantelisstampoulis.androidtemplateproject.domain.di.domainModule
import com.pantelisstampoulis.androidtemplateproject.logging.di.loggingModule
import com.pantelisstampoulis.androidtemplateproject.navigation.di.navigationModule
import org.koin.core.module.Module
import org.koin.dsl.module

val coreModule: Module = module {
    includes(
        domainModule,
        dataModule,
        dispatcherModule,
        loggingModule,
        navigationModule,
    )
}
