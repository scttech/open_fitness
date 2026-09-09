package com.scttech.android.kotlin.openfitness.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.scttech.android.kotlin.openfitness.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Query("SELECT * FROM workout_sessions WHERE profileId = :profileId ORDER BY startedAtEpochMillis DESC")
    fun observeSessionsForProfile(profileId: Long): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    fun observeSession(id: Long): Flow<WorkoutSessionEntity?>

    @Insert
    suspend fun insert(session: WorkoutSessionEntity): Long

    @Update
    suspend fun update(session: WorkoutSessionEntity)

    @Delete
    suspend fun delete(session: WorkoutSessionEntity)
}
