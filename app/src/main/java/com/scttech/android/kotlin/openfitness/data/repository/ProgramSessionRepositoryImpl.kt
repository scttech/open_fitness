package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.data.local.dao.ProgramSessionDao
import com.scttech.android.kotlin.openfitness.data.local.entity.asDomainModel
import com.scttech.android.kotlin.openfitness.data.local.entity.asEntity
import com.scttech.android.kotlin.openfitness.domain.model.ProgramSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgramSessionRepositoryImpl @Inject constructor(
    private val programSessionDao: ProgramSessionDao,
) : ProgramSessionRepository {

    override fun observeSessionsForProgram(programId: Long): Flow<List<ProgramSession>> =
        programSessionDao.observeSessionsForProgram(programId).map { entities -> entities.map { it.asDomainModel() } }

    override fun observeSessionsForProfile(profileId: Long): Flow<List<ProgramSession>> =
        programSessionDao.observeSessionsForProfile(profileId).map { entities -> entities.map { it.asDomainModel() } }

    override suspend fun countCompletedSessionsSince(programId: Long, since: Instant): Int =
        programSessionDao.countCompletedSessionsSince(programId, since.toEpochMilliseconds())

    override suspend fun startSession(session: ProgramSession): Long =
        programSessionDao.insert(session.asEntity())

    override suspend fun saveSession(session: ProgramSession) {
        programSessionDao.update(session.asEntity())
    }

    override suspend fun deleteSession(session: ProgramSession) {
        programSessionDao.delete(session.asEntity())
    }
}
