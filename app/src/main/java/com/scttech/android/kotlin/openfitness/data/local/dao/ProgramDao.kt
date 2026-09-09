package com.scttech.android.kotlin.openfitness.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.scttech.android.kotlin.openfitness.data.local.entity.ProgramEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {

    @Query("SELECT * FROM programs WHERE profileId = :profileId AND isArchived = 0 ORDER BY createdAtEpochMillis DESC")
    fun observeProgramsForProfile(profileId: Long): Flow<List<ProgramEntity>>

    @Query("SELECT * FROM programs WHERE id = :id")
    fun observeProgram(id: Long): Flow<ProgramEntity?>

    @Query("SELECT * FROM programs WHERE id = :id")
    suspend fun getProgram(id: Long): ProgramEntity?

    @Insert
    suspend fun insert(program: ProgramEntity): Long

    @Update
    suspend fun update(program: ProgramEntity)

    @Delete
    suspend fun delete(program: ProgramEntity)
}
