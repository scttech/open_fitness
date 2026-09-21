package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.domain.model.Profile
import com.scttech.android.kotlin.openfitness.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfiles(): Flow<List<Profile>>
    fun observeProfile(id: Long): Flow<Profile?>
    /** The active profile itself, tracking [currentProfileId] - null while none is selected. */
    fun observeCurrentProfile(): Flow<Profile?>
    val currentProfileId: Flow<Long?>
    suspend fun setCurrentProfile(profileId: Long?)
    /** App-wide light/dark preference, independent of which profile is active. */
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun createProfile(name: String, colorIndex: Int): Long
    suspend fun renameProfile(profile: Profile, newName: String)
    suspend fun deleteProfile(profile: Profile)
    suspend fun setTimerSoundEnabled(profileId: Long, enabled: Boolean)
    suspend fun setTimerWorkColor(profileId: Long, argb: Int)
    suspend fun setTimerRestColor(profileId: Long, argb: Int)
}
