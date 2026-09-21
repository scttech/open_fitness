package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.domain.model.ProgramTest
import kotlinx.coroutines.flow.Flow

interface ProgramRepository {
    fun observeProgramsForProfile(profileId: Long): Flow<List<Program>>
    fun observeProgram(id: Long): Flow<Program?>
    fun observeTestsForProgram(programId: Long): Flow<List<ProgramTest>>
    suspend fun saveProgram(program: Program): Long
    suspend fun deleteProgram(program: Program)

    /** Records a test, recalculates the prescription from it, and saves both atomically. */
    suspend fun recordTest(programId: Long, result: Int, notes: String = ""): Long
}
