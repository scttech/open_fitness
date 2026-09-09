package com.scttech.android.kotlin.openfitness.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.scttech.android.kotlin.openfitness.data.local.entity.ProgramSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramSessionDao {

    @Query("SELECT * FROM program_sessions WHERE programId = :programId ORDER BY startedAtEpochMillis DESC")
    fun observeSessionsForProgram(programId: Long): Flow<List<ProgramSessionEntity>>

    @Query("SELECT * FROM program_sessions WHERE profileId = :profileId ORDER BY startedAtEpochMillis DESC")
    fun observeSessionsForProfile(profileId: Long): Flow<List<ProgramSessionEntity>>

    @Insert
    suspend fun insert(session: ProgramSessionEntity): Long

    @Update
    suspend fun update(session: ProgramSessionEntity)

    @Delete
    suspend fun delete(session: ProgramSessionEntity)
}
