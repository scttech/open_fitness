package com.scttech.android.kotlin.openfitness.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.scttech.android.kotlin.openfitness.domain.model.PerformedSet
import com.scttech.android.kotlin.openfitness.domain.model.ProgramSession
import kotlinx.datetime.Instant

@Entity(
    tableName = "program_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("profileId"), Index("programId")],
)
data class ProgramSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val profileId: Long,
    val programId: Long?,
    val programName: String,
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long?,
    val loggedSets: List<PerformedSet>,
    val notes: String,
)

fun ProgramSessionEntity.asDomainModel() = ProgramSession(
    id = id,
    profileId = profileId,
    programId = programId ?: 0L,
    programName = programName,
    startedAt = Instant.fromEpochMilliseconds(startedAtEpochMillis),
    completedAt = completedAtEpochMillis?.let { Instant.fromEpochMilliseconds(it) },
    loggedSets = loggedSets,
    notes = notes,
)

fun ProgramSession.asEntity() = ProgramSessionEntity(
    id = id,
    profileId = profileId,
    programId = programId,
    programName = programName,
    startedAtEpochMillis = startedAt.toEpochMilliseconds(),
    completedAtEpochMillis = completedAt?.toEpochMilliseconds(),
    loggedSets = loggedSets,
    notes = notes,
)
