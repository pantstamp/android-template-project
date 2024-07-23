package com.pantelisstampoulis.androidtemplateproject

import android.app.Application
import com.pantelisstampoulis.core.di.initKoin
import org.koin.android.ext.koin.androidContext

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin().also { koinApplication ->
            koinApplication.androidContext(androidContext = this@MyApplication)
        }
    }
}
