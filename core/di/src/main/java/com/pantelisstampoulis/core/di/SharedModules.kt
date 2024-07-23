package com.pantelisstampoulis.core.di

import org.koin.core.module.Module
import org.koin.dsl.module

val sharedModule: Module = module {
    includes(
        // core
        //databaseModule,
        //dispatcherModule,
        //loggingModule,
        //networkModule,
        //preferencesModule,
        //serializationModule,
        //domainModule,
        //dataModule,
        // feature
        //accountsFeatureModule,
        //storeFeatureModule,
        //profileFeatureModule,
    )
}
