package com.scttech.android.kotlin.openfitness.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.scttech.android.kotlin.openfitness.domain.model.ProgramTest
import kotlinx.datetime.Instant

@Entity(
    tableName = "program_tests",
    foreignKeys = [
        ForeignKey(
            entity = ProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("programId"), Index("profileId")],
)
data class ProgramTestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val programId: Long,
    val profileId: Long,
    val testedAtEpochMillis: Long,
    val result: Double,
    val notes: String,
)

fun ProgramTestEntity.asDomainModel() = ProgramTest(
    id = id,
    programId = programId,
    profileId = profileId,
    testedAt = Instant.fromEpochMilliseconds(testedAtEpochMillis),
    result = result,
    notes = notes,
)

fun ProgramTest.asEntity() = ProgramTestEntity(
    id = id,
    programId = programId,
    profileId = profileId,
    testedAtEpochMillis = testedAt.toEpochMilliseconds(),
    result = result,
    notes = notes,
)
