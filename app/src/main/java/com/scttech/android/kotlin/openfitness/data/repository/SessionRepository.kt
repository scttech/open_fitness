package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeSessionsForProfile(profileId: Long): Flow<List<WorkoutSession>>
    fun observeSession(id: Long): Flow<WorkoutSession?>
    suspend fun startSession(session: WorkoutSession): Long
    suspend fun saveSession(session: WorkoutSession)
    suspend fun deleteSession(session: WorkoutSession)
}
