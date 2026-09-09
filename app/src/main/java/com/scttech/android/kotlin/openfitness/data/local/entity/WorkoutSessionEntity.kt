package com.scttech.android.kotlin.openfitness.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.scttech.android.kotlin.openfitness.domain.model.SessionResult
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutSession
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyle
import kotlinx.datetime.Instant

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("profileId"), Index("workoutId")],
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val profileId: Long,
    val workoutId: Long?,
    val workoutName: String,
    val style: WorkoutStyle,
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long?,
    val result: SessionResult?,
    val notes: String,
)

fun WorkoutSessionEntity.asDomainModel() = WorkoutSession(
    id = id,
    profileId = profileId,
    workoutId = workoutId ?: 0L,
    workoutName = workoutName,
    style = style,
    startedAt = Instant.fromEpochMilliseconds(startedAtEpochMillis),
    completedAt = completedAtEpochMillis?.let(Instant::fromEpochMilliseconds),
    result = result,
    notes = notes,
)

fun WorkoutSession.asEntity() = WorkoutSessionEntity(
    id = id,
    profileId = profileId,
    workoutId = workoutId,
    workoutName = workoutName,
    style = style,
    startedAtEpochMillis = startedAt.toEpochMilliseconds(),
    completedAtEpochMillis = completedAt?.toEpochMilliseconds(),
    result = result,
    notes = notes,
)
