package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfiles(): Flow<List<Profile>>
    fun observeProfile(id: Long): Flow<Profile?>
    val currentProfileId: Flow<Long?>
    suspend fun setCurrentProfile(profileId: Long?)
    suspend fun createProfile(name: String, colorIndex: Int): Long
    suspend fun renameProfile(profile: Profile, newName: String)
    suspend fun deleteProfile(profile: Profile)
}
