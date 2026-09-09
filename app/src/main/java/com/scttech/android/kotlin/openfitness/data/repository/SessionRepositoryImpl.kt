package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.data.local.dao.WorkoutSessionDao
import com.scttech.android.kotlin.openfitness.data.local.entity.asDomainModel
import com.scttech.android.kotlin.openfitness.data.local.entity.asEntity
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: WorkoutSessionDao,
) : SessionRepository {

    override fun observeSessionsForProfile(profileId: Long): Flow<List<WorkoutSession>> =
        sessionDao.observeSessionsForProfile(profileId).map { entities -> entities.map { it.asDomainModel() } }

    override fun observeSession(id: Long): Flow<WorkoutSession?> =
        sessionDao.observeSession(id).map { it?.asDomainModel() }

    override suspend fun startSession(session: WorkoutSession): Long =
        sessionDao.insert(session.asEntity())

    override suspend fun saveSession(session: WorkoutSession) {
        sessionDao.update(session.asEntity())
    }

    override suspend fun deleteSession(session: WorkoutSession) {
        sessionDao.delete(session.asEntity())
    }
}
