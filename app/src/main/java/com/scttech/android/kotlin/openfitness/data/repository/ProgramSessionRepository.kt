package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.domain.model.ProgramSession
import kotlinx.coroutines.flow.Flow

interface ProgramSessionRepository {
    fun observeSessionsForProgram(programId: Long): Flow<List<ProgramSession>>
    fun observeSessionsForProfile(profileId: Long): Flow<List<ProgramSession>>
    suspend fun startSession(session: ProgramSession): Long
    suspend fun saveSession(session: ProgramSession)
    suspend fun deleteSession(session: ProgramSession)
}
