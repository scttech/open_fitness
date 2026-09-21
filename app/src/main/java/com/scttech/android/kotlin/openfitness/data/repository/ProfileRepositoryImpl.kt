package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.data.datastore.UserPreferencesDataSource
import com.scttech.android.kotlin.openfitness.data.local.dao.ProfileDao
import com.scttech.android.kotlin.openfitness.data.local.entity.asDomainModel
import com.scttech.android.kotlin.openfitness.data.local.entity.asEntity
import com.scttech.android.kotlin.openfitness.domain.model.Profile
import com.scttech.android.kotlin.openfitness.domain.model.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao,
    private val preferencesDataSource: UserPreferencesDataSource,
) : ProfileRepository {

    override fun observeProfiles(): Flow<List<Profile>> =
        profileDao.observeProfiles().map { entities -> entities.map { it.asDomainModel() } }

    override fun observeProfile(id: Long): Flow<Profile?> =
        profileDao.observeProfile(id).map { it?.asDomainModel() }

    override fun observeCurrentProfile(): Flow<Profile?> =
        currentProfileId.flatMapLatest { id -> if (id == null) flowOf(null) else observeProfile(id) }

    override val currentProfileId: Flow<Long?> = preferencesDataSource.currentProfileId

    override suspend fun setCurrentProfile(profileId: Long?) {
        preferencesDataSource.setCurrentProfileId(profileId)
    }

    override val themeMode: Flow<ThemeMode> = preferencesDataSource.themeMode

    override suspend fun setThemeMode(mode: ThemeMode) {
        preferencesDataSource.setThemeMode(mode)
    }

    override suspend fun createProfile(name: String, colorIndex: Int): Long {
        val profile = Profile(name = name, colorIndex = colorIndex, createdAt = Clock.System.now())
        return profileDao.insert(profile.asEntity())
    }

    override suspend fun renameProfile(profile: Profile, newName: String) {
        profileDao.update(profile.copy(name = newName).asEntity())
    }

    override suspend fun deleteProfile(profile: Profile) {
        profileDao.delete(profile.asEntity())
    }

    override suspend fun setTimerSoundEnabled(profileId: Long, enabled: Boolean) {
        profileDao.updateTimerSoundEnabled(profileId, enabled)
    }

    override suspend fun setTimerWorkColor(profileId: Long, argb: Int) {
        profileDao.updateTimerWorkColorArgb(profileId, argb)
    }

    override suspend fun setTimerRestColor(profileId: Long, argb: Int) {
        profileDao.updateTimerRestColorArgb(profileId, argb)
    }
}
