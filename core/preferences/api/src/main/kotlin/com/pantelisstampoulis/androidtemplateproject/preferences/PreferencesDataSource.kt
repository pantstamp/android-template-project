package com.pantelisstampoulis.androidtemplateproject.preferences

import com.pantelisstampoulis.androidtemplateproject.preferences.model.UserSettingsPref
import kotlinx.coroutines.flow.Flow

interface PreferencesDataSource {

    val settings: Flow<UserSettingsPref?>

    suspend fun saveSettings(settings: UserSettingsPref)
}
