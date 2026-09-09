package com.scttech.android.kotlin.openfitness.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.scttech.android.kotlin.openfitness.data.local.entity.ProgramTestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramTestDao {

    @Query("SELECT * FROM program_tests WHERE programId = :programId ORDER BY testedAtEpochMillis DESC")
    fun observeTestsForProgram(programId: Long): Flow<List<ProgramTestEntity>>

    @Insert
    suspend fun insert(test: ProgramTestEntity): Long

    @Delete
    suspend fun delete(test: ProgramTestEntity)
}
