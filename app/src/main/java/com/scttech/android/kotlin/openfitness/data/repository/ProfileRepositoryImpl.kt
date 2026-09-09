package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.data.datastore.UserPreferencesDataSource
import com.scttech.android.kotlin.openfitness.data.local.dao.ProfileDao
import com.scttech.android.kotlin.openfitness.data.local.entity.asDomainModel
import com.scttech.android.kotlin.openfitness.data.local.entity.asEntity
import com.scttech.android.kotlin.openfitness.domain.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao,
    private val preferencesDataSource: UserPreferencesDataSource,
) : ProfileRepository {

    override fun observeProfiles(): Flow<List<Profile>> =
        profileDao.observeProfiles().map { entities -> entities.map { it.asDomainModel() } }

    override fun observeProfile(id: Long): Flow<Profile?> =
        profileDao.observeProfile(id).map { it?.asDomainModel() }

    override val currentProfileId: Flow<Long?> = preferencesDataSource.currentProfileId

    override suspend fun setCurrentProfile(profileId: Long?) {
        preferencesDataSource.setCurrentProfileId(profileId)
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
}
