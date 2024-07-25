package com.pantelisstampoulis.androidtemplateproject.preferences.di

import com.pantelisstampoulis.androidtemplateproject.preferences.NoopPreferencesDataSource
import com.pantelisstampoulis.androidtemplateproject.preferences.PreferencesDataSource
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

val preferencesModule: Module = module {

    single {
        NoopPreferencesDataSource()
    } bind PreferencesDataSource::class
}
