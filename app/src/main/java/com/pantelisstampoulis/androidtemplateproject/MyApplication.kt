package com.pantelisstampoulis.androidtemplateproject

import android.app.Application
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.di.featureMovieCatalogPresentationModule
import com.pantelisstampoulis.core.di.initKoin
import org.koin.android.ext.koin.androidContext

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // add here all the feature modules of the app
        val featureModules = listOf(
            featureMovieCatalogPresentationModule,
        )

        initKoin(
            featureModules,
        ).also { koinApplication ->
            koinApplication.androidContext(androidContext = this@MyApplication)
        }
    }
}
