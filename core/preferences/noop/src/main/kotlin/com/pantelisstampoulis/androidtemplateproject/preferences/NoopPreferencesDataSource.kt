package com.pantelisstampoulis.androidtemplateproject.preferences

import com.pantelisstampoulis.androidtemplateproject.preferences.model.UserSettingsPref
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class NoopPreferencesDataSource : PreferencesDataSource {

    override val settings: Flow<UserSettingsPref?> =
        flowOf(UserSettingsPref(favorites = emptySet()))

    override suspend fun saveSettings(settings: UserSettingsPref) {
        /* empty implementation */
    }
}
