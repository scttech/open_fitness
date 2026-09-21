package com.scttech.android.kotlin.openfitness.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.domain.model.ProgramConfig
import com.scttech.android.kotlin.openfitness.domain.model.ProgramGoalType
import com.scttech.android.kotlin.openfitness.domain.model.ProgramPrescription
import kotlinx.datetime.Instant

@Entity(
    tableName = "programs",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("profileId"), Index("exerciseId")],
)
data class ProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val profileId: Long,
    val name: String,
    val exerciseId: Long?,
    val exerciseName: String,
    val goalType: ProgramGoalType,
    val goalTarget: Int,
    val config: ProgramConfig,
    val currentPrescription: ProgramPrescription?,
    val lastTestResult: Int?,
    val lastTestedAtEpochMillis: Long?,
    val nextTestDueAtEpochMillis: Long?,
    val isArchived: Boolean,
    val createdAtEpochMillis: Long,
)

fun ProgramEntity.asDomainModel() = Program(
    id = id,
    profileId = profileId,
    name = name,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    goalType = goalType,
    goalTarget = goalTarget,
    config = config,
    currentPrescription = currentPrescription,
    lastTestResult = lastTestResult,
    lastTestedAt = lastTestedAtEpochMillis?.let { Instant.fromEpochMilliseconds(it) },
    nextTestDueAt = nextTestDueAtEpochMillis?.let { Instant.fromEpochMilliseconds(it) },
    isArchived = isArchived,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis),
)

fun Program.asEntity() = ProgramEntity(
    id = id,
    profileId = profileId,
    name = name,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    goalType = goalType,
    goalTarget = goalTarget,
    config = config,
    currentPrescription = currentPrescription,
    lastTestResult = lastTestResult,
    lastTestedAtEpochMillis = lastTestedAt?.toEpochMilliseconds(),
    nextTestDueAtEpochMillis = nextTestDueAt?.toEpochMilliseconds(),
    isArchived = isArchived,
    createdAtEpochMillis = createdAt.toEpochMilliseconds(),
)
