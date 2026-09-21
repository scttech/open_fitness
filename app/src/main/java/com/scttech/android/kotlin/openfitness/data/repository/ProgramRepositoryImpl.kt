package com.scttech.android.kotlin.openfitness.data.repository

import androidx.room.withTransaction
import com.scttech.android.kotlin.openfitness.data.local.OpenFitnessDatabase
import com.scttech.android.kotlin.openfitness.data.local.dao.ProgramDao
import com.scttech.android.kotlin.openfitness.data.local.dao.ProgramTestDao
import com.scttech.android.kotlin.openfitness.data.local.entity.asDomainModel
import com.scttech.android.kotlin.openfitness.data.local.entity.asEntity
import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.domain.model.ProgramTest
import com.scttech.android.kotlin.openfitness.domain.program.ProgramProgression
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgramRepositoryImpl @Inject constructor(
    private val database: OpenFitnessDatabase,
    private val programDao: ProgramDao,
    private val programTestDao: ProgramTestDao,
) : ProgramRepository {

    override fun observeProgramsForProfile(profileId: Long): Flow<List<Program>> =
        programDao.observeProgramsForProfile(profileId).map { entities -> entities.map { it.asDomainModel() } }

    override fun observeProgram(id: Long): Flow<Program?> =
        programDao.observeProgram(id).map { it?.asDomainModel() }

    override fun observeTestsForProgram(programId: Long): Flow<List<ProgramTest>> =
        programTestDao.observeTestsForProgram(programId).map { entities -> entities.map { it.asDomainModel() } }

    override suspend fun saveProgram(program: Program): Long {
        val toSave = if (program.id == 0L) program.copy(createdAt = Clock.System.now()) else program
        return if (toSave.id == 0L) {
            programDao.insert(toSave.asEntity())
        } else {
            programDao.update(toSave.asEntity())
            toSave.id
        }
    }

    override suspend fun deleteProgram(program: Program) {
        programDao.delete(program.asEntity())
    }

    override suspend fun recordTest(programId: Long, result: Int, notes: String): Long =
        database.withTransaction {
            val program = programDao.getProgram(programId)?.asDomainModel()
                ?: error("Program $programId not found")
            val now = Clock.System.now()
            val testId = programTestDao.insert(
                ProgramTest(programId = programId, profileId = program.profileId, testedAt = now, result = result, notes = notes)
                    .asEntity(),
            )
            val prescription = ProgramProgression.recalculate(program.config, result)
            programDao.update(
                program.copy(
                    currentPrescription = prescription,
                    lastTestResult = result,
                    lastTestedAt = now,
                    nextTestDueAt = now + program.config.retestIntervalDays.days,
                ).asEntity(),
            )
            testId
        }
}
