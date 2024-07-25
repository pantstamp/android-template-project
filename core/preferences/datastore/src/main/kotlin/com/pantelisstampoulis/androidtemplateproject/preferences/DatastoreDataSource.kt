package com.pantelisstampoulis.androidtemplateproject.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.pantelisstampoulis.androidtemplateproject.preferences.model.UserSettingsPref
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DatastoreDataSource(
    private val dataStore: DataStore<Preferences>,
) : PreferencesDataSource {

    // keys
    private val favoritesKey = stringSetPreferencesKey("favorites")

    override val settings: Flow<UserSettingsPref?> = dataStore.data.map { preferences ->
        preferences[favoritesKey]?.let { favorites ->
            UserSettingsPref(
                favorites = favorites,
            )
        }
    }

    override suspend fun saveSettings(settings: UserSettingsPref) {
        dataStore.edit { preferences ->
            preferences[favoritesKey] = settings.favorites
        }
    }
}
