package com.scttech.android.kotlin.openfitness.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.scttech.android.kotlin.openfitness.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val CURRENT_PROFILE_ID = longPreferencesKey("current_profile_id")
private val THEME_MODE = stringPreferencesKey("theme_mode")

/** Remembers which profile ("save slot") was last active, so the app can resume into it. */
@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val currentProfileId: Flow<Long?> = dataStore.data.map { it[CURRENT_PROFILE_ID] }

    suspend fun setCurrentProfileId(profileId: Long?) {
        dataStore.edit { prefs ->
            if (profileId == null) prefs.remove(CURRENT_PROFILE_ID) else prefs[CURRENT_PROFILE_ID] = profileId
        }
    }

    /** App-wide, independent of which profile is active. */
    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        prefs[THEME_MODE]?.let { name -> runCatching { ThemeMode.valueOf(name) }.getOrNull() } ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[THEME_MODE] = mode.name }
    }
}
