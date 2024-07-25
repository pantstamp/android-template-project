package com.pantelisstampoulis.androidtemplateproject.preferences.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.pantelisstampoulis.androidtemplateproject.dispatcher.CoroutinesDispatchers
import com.pantelisstampoulis.androidtemplateproject.preferences.DatastoreDataSource
import com.pantelisstampoulis.androidtemplateproject.preferences.PreferencesDataSource
import kotlinx.coroutines.CoroutineScope
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

val preferencesModule: Module = module {

    // todo revisit this
    single {
        val coroutineContext = get<CoroutineContext>(qualifier = named(CoroutinesDispatchers.IO))
        PreferenceDataStoreFactory.createWithPath(
            scope = CoroutineScope(context = coroutineContext),
            produceFile = {
                get<Context>()
                    .filesDir
                    .resolve("user_preferences.pb")
                    .absolutePath
                    .toPath()
            },
        )
    }


    single {
        DatastoreDataSource(
            dataStore = get(),
        )
    } bind PreferencesDataSource::class
}